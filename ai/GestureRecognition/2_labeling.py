import cv2
import mediapipe as mp
import numpy as np
from pathlib import Path
import yaml
from collections import defaultdict
from tqdm import tqdm
import random
import sys

class FinalAuctionHandLabeler:
    def __init__(self, raw_images_dir="./dataset/auction_hands",
                 output_dir="./dataset/yolo_auction_dataset",
                 test_images_dir="./dataset/testset"):
        
        self.raw_images_dir = Path(raw_images_dir)
        self.output_dir = Path(output_dir)
        self.test_images_dir = Path(test_images_dir)
        self.setup_yolo_structure()

        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=True, max_num_hands=1,
            min_detection_confidence=0.8, model_complexity=1
        )
        self.class_names = {
            "one": 0, "two": 1, "three": 2, "four": 3, "five": 4,
            "six": 5, "seven": 6, "eight": 7, "nine": 8, "ten": 9
        }
        self.quality_thresholds = {
            'min_hand_area': 3000, 'max_hand_area': 100000,
            'aspect_ratio_range': (0.5, 2.5), 'edge_distance': 10
        }
        tqdm.write("라벨링 시스템 초기화 완료.")

    def setup_yolo_structure(self):
        for split in ["train", "val", "test"]:
            (self.output_dir / "images" / split).mkdir(parents=True, exist_ok=True)
            (self.output_dir / "labels" / split).mkdir(parents=True, exist_ok=True)

    def apply_augmentations(self, image):
        """이미지에 무작위로 다양한 증강 기법을 적용합니다."""
        augmented_image = image.copy()
        h, w, _ = augmented_image.shape

        # --- [오류 수정] 크기 조절(Scaling) 로직 개선 ---
        if random.random() > 0.5:
            scale_factor = random.uniform(0.8, 1.2)
            new_w, new_h = int(w * scale_factor), int(h * scale_factor)
            resized = cv2.resize(image, (new_w, new_h))

            if scale_factor > 1.0: # 확대 (Zoom In)
                # 확대된 이미지의 중앙을 원본 크기로 자르기
                x_start = (new_w - w) // 2
                y_start = (new_h - h) // 2
                augmented_image = resized[y_start:y_start+h, x_start:x_start+w]
            else: # 축소 (Zoom Out)
                # 축소된 이미지를 회색 캔버스 중앙에 배치
                canvas = np.full((h, w, 3), 114, dtype=np.uint8)
                x_start = (w - new_w) // 2
                y_start = (h - new_h) // 2
                canvas[y_start:y_start+new_h, x_start:x_start+new_w] = resized
                augmented_image = canvas

        # 2. 밝기 조절 - 50% 확률
        if random.random() > 0.5:
            brightness_factor = random.uniform(0.5, 1.5)
            augmented_image = np.clip(augmented_image * brightness_factor, 0, 255).astype(np.uint8)

        # 4. 가우시안 블러 (Blur) - 30% 확률
        if random.random() > 0.7:
            ksize = random.choice([3, 5, 7])
            augmented_image = cv2.GaussianBlur(augmented_image, (ksize, ksize), 0)

        # 5. 가우시안 노이즈 (Noise) - 30% 확률
        if random.random() > 0.7:
            noise = np.random.normal(0, 15, augmented_image.shape)
            augmented_image = np.clip(augmented_image + noise, 0, 255).astype(np.uint8)

        # 6. 컷아웃 (Cutout) - 40% 확률
        if random.random() > 0.6:
            cutout_h = int(random.uniform(0.05, 0.2) * h)
            cutout_w = int(random.uniform(0.05, 0.2) * w)
            x1 = random.randint(0, w - cutout_w)
            y1 = random.randint(0, h - cutout_h)
            augmented_image[y1:y1+cutout_h, x1:x1+cutout_w] = (114, 114, 114)

        # 7. 좌우 반전 - 50% 확률
        if random.random() > 0.5:
            augmented_image = cv2.flip(augmented_image, 1)

        return augmented_image

    def detect_hand(self, image):
        if image is None: return None
        h, w, _ = image.shape
        results = self.hands.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        if not results.multi_hand_landmarks: return None
        
        hand_landmarks = results.multi_hand_landmarks[0]
        x_coords = [lm.x * w for lm in hand_landmarks.landmark]
        y_coords = [lm.y * h for lm in hand_landmarks.landmark]
        x_min, x_max = min(x_coords), max(x_coords)
        y_min, y_max = min(y_coords), max(y_coords)
        
        if not self._check_quality(x_min, x_max, y_min, y_max, w, h):
            return None
            
        x_center = (x_min + x_max) / 2.0 / w
        y_center = (y_min + y_max) / 2.0 / h
        bbox_width = (x_max - x_min) / w
        bbox_height = (y_max - y_min) / h
        
        return [x_center, y_center, bbox_width, bbox_height]
    
    def _check_quality(self, x_min, x_max, y_min, y_max, width, height):
        area = (x_max - x_min) * (y_max - y_min)
        if not (self.quality_thresholds['min_hand_area'] < area < self.quality_thresholds['max_hand_area']):
            return False
        ratio = (x_max - x_min) / (y_max - y_min) if (y_max - y_min) > 0 else 0
        min_r, max_r = self.quality_thresholds['aspect_ratio_range']
        if not (min_r < ratio < max_r):
            return False
        edge = self.quality_thresholds['edge_distance']
        if (x_min < edge or y_min < edge or x_max > width - edge or y_max > height - edge):
            return False
        return True

    def run_processing_memory_safe(self, train_ratio=0.9, augmentations_per_image=10):
        all_tasks = []
        all_files_by_class = defaultdict(list)
        
        for class_name in self.class_names.keys():
            class_dir = self.raw_images_dir / class_name
            if class_dir.exists():
                files = list(class_dir.glob("*.jpg")) + list(class_dir.glob("*.png"))
                all_files_by_class[class_name] = files

        for class_name, files in all_files_by_class.items():
            random.shuffle(files)
            n_train = int(len(files) * train_ratio)
            all_tasks.extend([(p, class_name, 'train') for p in files[:n_train]])
            all_tasks.extend([(p, class_name, 'val') for p in files[n_train:]])
        
        if self.test_images_dir.exists():
            for class_name in self.class_names.keys():
                class_dir = self.test_images_dir / class_name
                if class_dir.exists():
                    files = list(class_dir.glob("*.jpg")) + list(class_dir.glob("*.png"))
                    all_tasks.extend([(p, class_name, 'test') for p in files])
        
        if not all_tasks:
            tqdm.write("처리할 이미지가 없습니다.")
            return

        file_counters = {'train': 0, 'val': 0, 'test': 0}
        for img_path, class_name, split in tqdm(all_tasks, desc="전체 데이터 처리 중", file=sys.stdout):
            try:
                original_image = cv2.imread(str(img_path))
                if original_image is None: continue

                bbox = self.detect_hand(original_image)
                if bbox:
                    self._save_single_item(original_image, bbox, class_name, file_counters[split], split, "original")
                    file_counters[split] += 1
                
                if split == 'train':
                    for i in range(augmentations_per_image):
                        aug_image = self.apply_augmentations(original_image)
                        aug_bbox = self.detect_hand(aug_image)
                        if aug_bbox:
                            self._save_single_item(aug_image, aug_bbox, class_name, file_counters[split], split, f"aug_{i}")
                            file_counters[split] += 1
            
            except Exception as e:
                tqdm.write(f"\n[오류] {img_path} 처리 중 오류: {e}")
                continue

        self.create_dataset_yaml()

    def _save_single_item(self, image, bbox, class_name, index, split, suffix):
        class_id = self.class_names[class_name]
        fname = f"{class_name}_{split}_{index:05d}_{Path(suffix).stem}"
        img_path = self.output_dir / "images" / split / f"{fname}.jpg"
        label_path = self.output_dir / "labels" / split / f"{fname}.txt"
        
        cv2.imwrite(str(img_path), image)
        with open(label_path, 'w') as f:
            f.write(f"{class_id} {bbox[0]:.6f} {bbox[1]:.6f} {bbox[2]:.6f} {bbox[3]:.6f}\n")

    def create_dataset_yaml(self):
        content = {
            'path': str(self.output_dir.absolute()),
            'train': 'images/train', 'val': 'images/val', 'test': 'images/test',
            'nc': len(self.class_names),
            'names': {v: k for k, v in self.class_names.items()}
        }
        with open(self.output_dir / "dataset.yaml", 'w', encoding='utf-8') as f:
            yaml.dump(content, f, default_flow_style=False, allow_unicode=True)

def main():
    try:
        labeler = FinalAuctionHandLabeler(
            raw_images_dir="./dataset/auction_hands",
            output_dir="./dataset/yolo_auction_dataset",
            test_images_dir="./dataset/testset"
        )
        labeler.run_processing_memory_safe(augmentations_per_image=10)
        tqdm.write("\n모든 라벨링 및 데이터셋 생성이 성공적으로 완료되었습니다!")
    except Exception as e:
        tqdm.write(f"\n[치명적 오류] 스크립트 실행 중 오류: {e}")

if __name__ == "__main__":
    main()
