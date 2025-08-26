import cv2
import torch
import yaml
import time
import mediapipe as mp
import numpy as np
import random
from ultralytics import YOLO
from pathlib import Path

# =================================================================
# --- 설정 (여기서 파라미터를 직접 수정하세요) ---
MODEL_PATH = "./results/train_set/weights/best.pt"
DATA_YAML_PATH = "./dataset/yolo_auction_dataset/dataset.yaml"
CONF_THRESHOLD = 0.6
WEBCAM_ID = 0
# =================================================================

class FinalInferenceWithColors:
    """
    클래스별 색상 구분을 적용한 최종 추론 클래스
    """
    def __init__(self, model_path, data_yaml_path):
        self.model_path = Path(model_path)
        self.data_yaml_path = Path(data_yaml_path)
        self.mask_dilation_kernel_size = 50

        if not self.model_path.is_file():
            print(f"모델 파일을 찾을 수 없습니다: {self.model_path.absolute()}")
            raise FileNotFoundError(f"Model file not found at: {self.model_path.absolute()}")

        self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"사용 디바이스: {self.device.upper()}")
        
        self.model = YOLO(self.model_path)
        self.class_names = self._load_class_names()
        
        self.color_map = self._create_color_map()
        print(f"클래스별 색상 맵 생성 완료.")
        
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False, max_num_hands=1,
            min_detection_confidence=0.5, min_tracking_confidence=0.5
        )
        self.mp_drawing = mp.solutions.drawing_utils
        print("최종 추론 시스템 초기화 완료.")

    def _load_class_names(self):
        """YAML 파일에서 클래스 이름을 로드합니다."""
        if not self.data_yaml_path.is_file():
            raise FileNotFoundError(f"YAML file not found at: {self.data_yaml_path.absolute()}")
        with open(self.data_yaml_path, 'r', encoding='utf-8') as f:
            data_config = yaml.safe_load(f)
        return data_config['names']

    def _create_color_map(self):
        """클래스 수만큼 랜덤 색상을 생성하여 딕셔너리로 반환합니다."""
        random.seed(42) 
        colors = {}
        for class_id in range(len(self.class_names)):
            colors[class_id] = (random.randint(50, 255), random.randint(50, 255), random.randint(50, 255))
        return colors

    def _create_segmented_image(self, frame, landmarks):
        """모델 예측에 사용할 배경 제거 이미지를 생성합니다."""
        height, width, _ = frame.shape
        x_coords = [lm.x * width for lm in landmarks.landmark]
        y_coords = [lm.y * height for lm in landmarks.landmark]
        x_min, x_max = min(x_coords), max(x_coords)
        y_min, y_max = min(y_coords), max(y_coords)
        
        margin_x = int((x_max - x_min) * 0.4)
        margin_y = int((y_max - y_min) * 0.4)
        
        bbox_x_min = max(0, int(x_min - margin_x))
        bbox_x_max = min(width, int(x_max + margin_x))
        bbox_y_min = max(0, int(y_min - margin_y))
        bbox_y_max = min(height, int(y_max + margin_y))
        
        roi = frame[bbox_y_min:bbox_y_max, bbox_x_min:bbox_x_max]
        if roi.size == 0:
            return None, None

        landmark_points_relative = np.array(
            [[lm.x * width - bbox_x_min, lm.y * height - bbox_y_min] for lm in landmarks.landmark],
            dtype=np.int32
        )
        hull = cv2.convexHull(landmark_points_relative)
        base_mask = np.zeros(roi.shape[:2], dtype=np.uint8)
        cv2.fillConvexPoly(base_mask, hull, 255)

        kernel = np.ones((self.mask_dilation_kernel_size, self.mask_dilation_kernel_size), np.uint8)
        final_mask = cv2.dilate(base_mask, kernel, iterations=1)

        segmented_image = cv2.bitwise_and(roi, roi, mask=final_mask)
        bbox = (bbox_x_min, bbox_y_min, bbox_x_max, bbox_y_max)
        return segmented_image, bbox

    def _draw_predictions_on_original_frame(self, original_frame, yolo_results, mp_results, fps):
        """예측 결과를 클래스별 색상으로 원본 프레임 위에 그려 최종 화면을 생성합니다."""
        display_frame = original_frame.copy()

        if mp_results.multi_hand_landmarks:
            self.mp_drawing.draw_landmarks(display_frame, mp_results.multi_hand_landmarks[0], self.mp_hands.HAND_CONNECTIONS)

        if len(yolo_results[0].boxes) > 0:
            for box in yolo_results[0].boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                confidence = float(box.conf[0])
                class_id = int(box.cls[0])
                class_name = self.class_names.get(class_id, "UNKNOWN")
                
                color = self.color_map.get(class_id, (0, 255, 0)) # 기본값은 녹색
                
                # 가져온 색상으로 바운딩 박스와 라벨 그리기
                cv2.rectangle(display_frame, (x1, y1), (x2, y2), color, 3) # 선 굵기 증가
                label = f"{class_name}: {confidence:.2f}"
                
                # 라벨 텍스트 배경 추가
                (w, h), _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.9, 2)
                cv2.rectangle(display_frame, (x1, y1 - h - 15), (x1 + w, y1 - 10), color, -1)
                cv2.putText(display_frame, label, (x1, y1 - 15), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2) # 텍스트는 흰색

        cv2.putText(display_frame, f"FPS: {fps:.1f}", (20, 40), 
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        return display_frame

    def run_webcam_inference(self, webcam_id, conf_threshold):
        """지정된 웹캠을 사용하여 실시간 추론을 실행합니다."""
        cap = cv2.VideoCapture(webcam_id)
        if not cap.isOpened():
            print(f"웹캠(ID: {webcam_id})을 열 수 없습니다.")
            return

        print("최종 데모를 시작합니다. 'q' 키를 누르면 종료됩니다.")
        prev_time = 0
        
        while True:
            ret, frame = cap.read()
            if not ret: break
            
            frame = cv2.flip(frame, 1)
            curr_time = time.time()
            fps = 1 / (curr_time - prev_time) if (curr_time - prev_time) > 0 else 0
            prev_time = curr_time

            rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            mp_results = self.hands.process(rgb_frame)
            input_for_yolo = np.zeros_like(frame)
            if mp_results.multi_hand_landmarks:
                segmented_image, bbox = self._create_segmented_image(frame, mp_results.multi_hand_landmarks[0])
                if segmented_image is not None:
                    input_for_yolo[bbox[1]:bbox[3], bbox[0]:bbox[2]] = segmented_image
            
            yolo_results = self.model(input_for_yolo, conf=conf_threshold, device=self.device, verbose=False)

            final_display_frame = self._draw_predictions_on_original_frame(
                frame, yolo_results, mp_results, fps
            )
            
            cv2.imshow("Auction Hand Demo - Class Colors", final_display_frame)

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

        cap.release()
        cv2.destroyAllWindows()
        self.hands.close()
        print("스트림 처리가 정상적으로 종료되었습니다.")

def main():
    try:
        inference = FinalInferenceWithColors(
            model_path=MODEL_PATH,
            data_yaml_path=DATA_YAML_PATH
        )
        inference.run_webcam_inference(
            webcam_id=WEBCAM_ID,
            conf_threshold=CONF_THRESHOLD
        )
    except Exception as e:
        print(f"스크립트 실행 중 오류가 발생했습니다: {e}")

if __name__ == "__main__":
    main()
