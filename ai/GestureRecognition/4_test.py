import yaml
import torch
import pandas as pd
import cv2
import random
from ultralytics import YOLO
from pathlib import Path

# =================================================================
# --- 설정 (여기서 파라미터를 직접 수정하세요) ---

# 1. 평가할 훈련된 모델(.pt) 파일 경로
MODEL_PATH = "./results/train_set/weights/best.pt"

# 2. 데이터셋 설정(dataset.yaml) 파일 경로
DATA_YAML_PATH = "./dataset/yolo_auction_dataset/dataset.yaml"

# 3. 평가할 데이터 분할 ('train', 'val', 'test' 중 선택)
EVALUATION_SPLIT = "test"

# =================================================================

class AuctionHandEvaluator:
    """
    훈련된 경매 손동작 인식 모델의 성능을 평가하고 결과를 시각화합니다.
    """
    def __init__(self, model_path, data_yaml_path):
        self.model_path = Path(model_path)
        self.data_yaml_path = Path(data_yaml_path)

        if not self.model_path.exists():
            raise FileNotFoundError(f"모델 파일을 찾을 수 없습니다: {self.model_path}")
        if not self.data_yaml_path.exists():
            raise FileNotFoundError(f"데이터 YAML 파일을 찾을 수 없습니다: {self.data_yaml_path}")
            
        self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"사용 디바이스: {self.device.upper()}")
        
        self.model = YOLO(self.model_path)
        print(f"모델 로드 완료: {self.model_path}")

        with self.data_yaml_path.open('r', encoding='utf-8') as f:
            self.data_config = yaml.safe_load(f)
        self.class_names = self.data_config['names']
        print(f"클래스 정보 로드 완료: {self.class_names}")

    def run_evaluation(self, split='test'):
        """지정된 데이터셋 분할에 대해 모델 성능을 평가합니다."""
        print(f"===== '{split}' 데이터셋에 대한 모델 평가 시작 =====")
        
        project_path = "./results"
        run_name = f"{self.model_path.stem}_on_{split}_set"
        
        metrics = self.model.val(
            data=str(self.data_yaml_path), split=split, imgsz=640,
            batch=16, device=self.device, plots=True, save_json=True,
            project=project_path, name=run_name, exist_ok=True
        )
        
        print("모델 평가가 완료되었습니다.")
        print(f"결과 플롯과 JSON 파일이 '{metrics.save_dir}'에 저장되었습니다.")
        return metrics

    def generate_performance_report(self, metrics):
        """
        평가 메트릭을 기반으로 상세한 성능 리포트를 생성합니다. (API 변경 최종 대응)
        """
        print("\n===== 성능 평가 리포트 =====")
        summary = {
            "mAP50-95(B)": f"{metrics.box.map:.4f}",
            "mAP50(B)": f"{metrics.box.map50:.4f}",
            "mAP75(B)": f"{metrics.box.map75:.4f}",
            "Precision(B)": f"{metrics.box.mp:.4f}",
            "Recall(B)": f"{metrics.box.mr:.4f}",
        }
        print("--- 전체 성능 요약 ---")
        for key, value in summary.items():
            print(f"{key:<15}: {value}")
            
        report_data = []
        precisions = metrics.box.p
        recalls = metrics.box.r
        map50s = metrics.box.ap50   
        map_all = metrics.box.maps  

        for i, name in self.class_names.items():
            report_data.append({
                'Class': name,
                'Precision(B)': precisions[i],
                'Recall(B)': recalls[i],
                'mAP50(B)': map50s[i],
                'mAP50-95(B)': map_all[i],
            })
        
        df = pd.DataFrame(report_data)
        
        pd.set_option('display.max_rows', None)
        pd.set_option('display.width', 120)
        
        print("\n--- 클래스별 상세 성능 ---")
        print(df.to_string(index=False, float_format="%.4f"))
        print("=" * 60)

def main():
    """메인 실행 함수: 설정된 파라미터로 모델 평가 파이프라인을 실행합니다."""
    try:
        evaluator = AuctionHandEvaluator(
            model_path=MODEL_PATH,
            data_yaml_path=DATA_YAML_PATH
        )
        
        metrics = evaluator.run_evaluation(split=EVALUATION_SPLIT)
        evaluator.generate_performance_report(metrics)
        
        print("모든 평가 프로세스가 성공적으로 완료되었습니다.")

    except (FileNotFoundError, Exception) as e:
        print(f"오류가 발생했습니다: {e}")

if __name__ == "__main__":
    main()
