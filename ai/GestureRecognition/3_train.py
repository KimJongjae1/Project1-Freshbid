import torch
import pandas as pd
import matplotlib.pyplot as plt
from ultralytics import YOLO
from pathlib import Path

class AuctionHandTrainer:
    """
    경매 손동작 인식 모델의 훈련, 검증, 내보내기 및 시각화를 관리하는 클래스입니다.
    
    주요 기능:
    - YOLOv8 모델 훈련 및 하이퍼파라미터 튜닝
    - 훈련된 모델의 성능 자동 검증
    - 실시간 추론을 위한 ONNX 형식으로 모델 내보내기
    - 훈련 결과(손실, mAP 등)를 시각화하여 분석
    """
    def __init__(self, dataset_path, model_size="yolov8s.pt"):
        """
        AuctionHandTrainer 초기화
        
        Args:
            dataset_path (str): YOLO 형식의 데이터셋 경로
            model_size (str): 사용할 YOLOv8 모델 크기 (e.g., "yolov8n.pt", "yolov8s.pt")
        """
        self.dataset_path = Path(dataset_path)
        self.model_size = model_size
        self.data_yaml_path = self.dataset_path / "dataset.yaml"
        self.project_dir = Path("./results")
        
        self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"사용 디바이스: {self.device.upper()}")

        self._verify_dataset()
        self.model = YOLO(self.model_size)

    def _verify_dataset(self):
        """데이터셋 경로와 `dataset.yaml` 파일의 유효성을 검사합니다."""
        if not self.dataset_path.exists():
            print(f"데이터셋 경로를 찾을 수 없습니다: {self.dataset_path}")
            raise FileNotFoundError(f"Dataset path not found: {self.dataset_path}")
        if not self.data_yaml_path.exists():
            print(f"dataset.yaml 파일을 찾을 수 없습니다: {self.data_yaml_path}")
            raise FileNotFoundError(f"YAML file not found: {self.data_yaml_path}")
        print("데이터셋 경로 및 설정 파일 확인 완료.")

    def train(self, epochs=5, batch=-1, imgsz=640, patience=25, lr0=0.01):
        """
        모델을 훈련합니다.
        
        Args:
            epochs (int): 총 훈련 에포크 수
            batch (int): 배치 크기 (-1은 자동 조절)
            imgsz (int): 입력 이미지 크기
            patience (int): 조기 종료 (Early Stopping)를 위한 대기 에포크 수
            lr0 (float): 초기 학습률
        
        Returns:
            ultralytics.yolo.engine.results.Results: 훈련 결과 객체
        """
        print("모델 훈련을 시작합니다...")
        
        results = self.model.train(
            data=str(self.data_yaml_path),
            project=str(self.project_dir),
            name="train_set",
            epochs=epochs,
            batch=batch,
            imgsz=imgsz,
            patience=patience,
            device=self.device,
            lr0=lr0,
            momentum=0.937,
            weight_decay=0.0005,
            warmup_epochs=3.0,
            box=7.5,
            cls=0.5,
            dfl=1.5,
            plots=True, # 훈련 중 성능 그래프 생성
            save=True,
            exist_ok=True, # 동일한 이름의 프로젝트가 있어도 덮어쓰기
        )
        
        print("모델 훈련이 완료되었습니다.")
        print(f"최종 모델은 '{results.save_dir}'에 저장되었습니다.")
        self.best_model_path = results.save_dir / 'weights' / 'best.pt'
        self.results_dir_path = results.save_dir
        return results

    def hyperparameter_tuning(self, iterations=100, epochs=5):
        """
        YOLOv8의 내장 튜닝 기능을 사용하여 최적의 하이퍼파라미터를 탐색합니다.
        
        Args:
            iterations (int): 튜닝 시도 횟수
            epochs (int): 각 튜닝 시도별 에포크 수
        """
        print("하이퍼파라미터 튜닝을 시작합니다...")
        self.model.tune(
            data=str(self.data_yaml_path),
            project=str(self.project_dir),
            name="hyperparameter_tuning",
            iterations=iterations,
            epochs=epochs,
            optimizer='AdamW',
            plots=False,
            save=False,
            val=False,
            device=self.device
        )
        print("하이퍼파라미터 튜닝이 완료되었습니다.")

    def validate(self):
        """훈련된 최적의 모델(best.pt)을 사용하여 검증 데이터셋으로 성능을 평가합니다."""
        if not hasattr(self, 'best_model_path') or not self.best_model_path.exists():
            print("검증할 모델을 찾을 수 없습니다. 먼저 훈련을 완료하세요.")
            return

        print(f"'{self.best_model_path}' 모델로 검증을 시작합니다...")
        model = YOLO(self.best_model_path)
        metrics = model.val(
            data=str(self.data_yaml_path),
            imgsz=640,
            split='val',
            device=self.device,
            plots=True,
            project=str(self.project_dir),
            name="valid_set",
            exist_ok=True
        )
        print("모델 검증이 완료되었습니다.")
        print(f"mAP50-95: {metrics.box.map:.4f}, mAP50: {metrics.box.map50:.4f}")
        return metrics

    def export_model(self):
        """최적의 모델을 ONNX 형식으로 내보냅니다."""
        if not hasattr(self, 'best_model_path') or not self.best_model_path.exists():
            print("내보낼 모델을 찾을 수 없습니다. 먼저 훈련을 완료하세요.")
            return

        print(f"'{self.best_model_path}' 모델을 ONNX 형식으로 내보냅니다...")
        model = YOLO(self.best_model_path)
        model.export(format='onnx', imgsz=640)
        print("모델을 ONNX 형식으로 성공적으로 내보냈습니다.")

    def plot_training_results(self):
        """훈련 과정에서 생성된 'results.csv' 파일을 읽어 성능 지표를 시각화합니다."""
        if not hasattr(self, 'results_dir_path'):
            print("결과 디렉토리를 찾을 수 없습니다. 시각화를 건너뜁니다.")
            return

        results_csv_path = self.results_dir_path / 'results.csv'
        if not results_csv_path.exists():
            print(f"'{results_csv_path}' 파일을 찾을 수 없어 시각화를 건너뜁니다.")
            return

        print("훈련 결과 시각화를 시작합니다...")
        results_df = pd.read_csv(results_csv_path)
        # 컬럼 이름의 공백 제거
        results_df.columns = results_df.columns.str.strip()

        fig, axs = plt.subplots(2, 2, figsize=(12, 10))
        fig.suptitle('YOLOv8 Training & Validation Metrics', fontsize=16)
        
        # 손실 함수 그래프
        axs[0, 0].plot(results_df['epoch'], results_df['train/box_loss'], label='Train Box Loss')
        axs[0, 0].plot(results_df['epoch'], results_df['val/box_loss'], label='Validation Box Loss')
        axs[0, 0].set_title('Box Loss per Epoch')
        axs[0, 0].set_xlabel('Epoch')
        axs[0, 0].set_ylabel('Loss')
        axs[0, 0].legend()
        axs[0, 0].grid(True)
        
        # mAP 그래프
        axs[0, 1].plot(results_df['epoch'], results_df['metrics/mAP50(B)'], label='mAP@0.5')
        axs[0, 1].plot(results_df['epoch'], results_df['metrics/mAP50-95(B)'], label='mAP@0.5:0.95')
        axs[0, 1].set_title('mAP Score per Epoch')
        axs[0, 1].set_xlabel('Epoch')
        axs[0, 1].set_ylabel('mAP')
        axs[0, 1].legend()
        axs[0, 1].grid(True)

        # 정밀도(Precision) 및 재현율(Recall) 그래프
        axs[1, 0].plot(results_df['epoch'], results_df['metrics/precision(B)'], label='Precision')
        axs[1, 0].plot(results_df['epoch'], results_df['metrics/recall(B)'], label='Recall')
        axs[1, 0].set_title('Precision and Recall per Epoch')
        axs[1, 0].set_xlabel('Epoch')
        axs[1, 0].set_ylabel('Score')
        axs[1, 0].legend()
        axs[1, 0].grid(True)

        # 학습률(Learning Rate) 그래프
        axs[1, 1].plot(results_df['epoch'], results_df['lr/pg0'], label='Learning Rate Group 0')
        axs[1, 1].plot(results_df['epoch'], results_df['lr/pg1'], label='Learning Rate Group 1')
        axs[1, 1].plot(results_df['epoch'], results_df['lr/pg2'], label='Learning Rate Group 2')
        axs[1, 1].set_title('Learning Rate per Epoch')
        axs[1, 1].set_xlabel('Epoch')
        axs[1, 1].set_ylabel('Learning Rate')
        axs[1, 1].legend()
        axs[1, 1].grid(True)

        plt.tight_layout(rect=[0, 0.03, 1, 0.95])
        
        # 시각화 결과 저장
        save_path = self.results_dir_path / 'custom_training_summary.png'
        plt.savefig(save_path)
        print(f"훈련 결과 그래프가 '{save_path}'에 저장되었습니다.")

def main():
    dataset = "./dataset/yolo_auction_dataset"
    model = "yolov8n.pt"
    epoch = 10
    try:
        trainer = AuctionHandTrainer(dataset_path=dataset, model_size=model)
        
        # trainer.hyperparameter_tuning()

        trainer.train(epochs=epoch)
        trainer.validate()
        trainer.export_model()
        trainer.plot_training_results()
            
        print("모든 프로세스가 성공적으로 완료되었습니다.")

    except (FileNotFoundError, Exception) as e:
        print(f"오류가 발생했습니다: {e}")

if __name__ == "__main__":
    main()
