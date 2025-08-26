import cv2
import mediapipe as mp
import numpy as np
from datetime import datetime
from pathlib import Path

class FinalDataCollector:
    def __init__(self, save_dir="./dataset/auction_hands",
                 min_detection_confidence=0.8,
                 target_images_per_class=300):
        
        self.save_dir = Path(save_dir)
        self.target_images_per_class = target_images_per_class
        self.mask_dilation_kernel_size = 50

        self.current_class_idx = 0
        self.class_names = {
            0: "one", 1: "two", 2: "three", 3: "four", 4: "five",
            5: "six", 6: "seven", 7: "eight", 8: "nine", 9: "ten"
        }
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False, max_num_hands=1,
            min_detection_confidence=min_detection_confidence,
            min_tracking_confidence=0.8
        )
        self.mp_drawing = mp.solutions.drawing_utils
        self.stats = {name: 0 for name in self.class_names.values()}
        for name in self.class_names.values():
            (self.save_dir / name).mkdir(parents=True, exist_ok=True)
            
        print("키보드 입력이 수정된 최종 데이터 수집기 초기화 완료.")

    def _create_segmented_image(self, frame, landmarks):
        """하나의 함수에서 ROI 추출, 마스크 생성, 팽창, 배경 제거를 모두 처리합니다."""
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

    def save_image(self, image, class_name):
        """캡처된 최종 이미지를 파일로 저장합니다."""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")[:-3]
        filename = f"{class_name}_{timestamp}.jpg"
        save_path = self.save_dir / class_name / filename
        cv2.imwrite(str(save_path), image)
        
        self.stats[class_name] += 1
        print(f"저장 완료: {save_path} ({self.stats[class_name]}개)")

    def collect_data(self, camera_id=0):
        """웹캠을 통해 데이터를 수집하는 메인 루프입니다."""
        cap = cv2.VideoCapture(camera_id)
        if not cap.isOpened():
            print(f"카메라(ID: {camera_id})를 열 수 없습니다.")
            return

        print("데이터 수집을 시작합니다. ('q' 키로 종료)")
        
        while True:
            ret, frame = cap.read()
            if not ret: break

            frame = cv2.flip(frame, 1)
            display_frame = frame.copy()
            segmented_image_to_save = None  # 저장할 이미지를 담을 변수
            
            results = self.hands.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))

            if results.multi_hand_landmarks:
                hand_landmarks = results.multi_hand_landmarks[0]
                segmented_image, bbox = self._create_segmented_image(frame, hand_landmarks)
                segmented_image_to_save = segmented_image # 저장 후보로 지정

                if segmented_image is not None:
                    cv2.rectangle(display_frame, (bbox[0], bbox[1]), (bbox[2], bbox[3]), (0, 255, 0), 2)
                    self.mp_drawing.draw_landmarks(display_frame, hand_landmarks, self.mp_hands.HAND_CONNECTIONS)
                    cv2.imshow("Segmented Preview", segmented_image)
            
            current_class = self.class_names[self.current_class_idx]
            text = f"Class: {current_class.upper()} ({self.stats[current_class]}/{self.target_images_per_class})"
            cv2.putText(display_frame, text, (20, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2)
            cv2.imshow('Data Collection', display_frame)

            key = cv2.waitKey(1) & 0xFF

            if key == ord('q'):
                break
            
            elif key == ord('n'): # 다음 클래스
                self.current_class_idx = (self.current_class_idx + 1) % len(self.class_names)
                print(f"클래스 변경 -> {self.class_names[self.current_class_idx].upper()}")

            elif key == ord('p'): # 이전 클래스
                self.current_class_idx = (self.current_class_idx - 1 + len(self.class_names)) % len(self.class_names)
                print(f"클래스 변경 -> {self.class_names[self.current_class_idx].upper()}")

            elif key == ord(' '): # 이미지 저장
                if segmented_image_to_save is not None:
                    self.save_image(segmented_image_to_save, self.class_names[self.current_class_idx])
                else:
                    print("저장 실패: 감지된 손이 없습니다.")

        cap.release()
        cv2.destroyAllWindows()
        print("데이터 수집이 종료되었습니다.")

def main():
    collector = FinalDataCollector()
    collector.collect_data()

if __name__ == "__main__":
    main()
