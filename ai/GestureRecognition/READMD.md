# YOLOv8 경매 손동작 인식 모델 구축 프로젝트

### env setting

```bash
conda create -n yolo_env python=3.9
# conda env list
conda activate yolo_env
pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121
pip install -r requirement.txt
```

### 실행 순서

1단계: 데이터 수집

```bash
python 1_data_collection.py
```

- 웹캠을 통해 경매 손동작 데이터를 수집합니다
- 각 숫자1-10당 최소 1000장 이상 수집을 권장합니다

2단계: 데이터 라벨링

```bash
python 2_labeling.py
```

- MediaPipe를 이용한 자동 라벨링 또는 수동 라벨링 선택
- 훈련/검증/테스트 데이터셋 자동 분할
- 회전, 반전, 블러 등 다양한 증강기법 활용

3단계: 모델 훈련

```bash
python 3_train.py
```

- YOLOv8 모델 훈련 실행
- 훈련 과정 모니터링 및 최적 모델 저장
- 훈련 결과 시각화

결과는 results/train_set/weight 에 저장됩니다

4단계: 모델 평가

```bash
python 4_test.py
```

- F1 Score 기반 성능 평가
- 클래스별 정밀도, 재현율, F1 점수 계산
- 혼동 행렬 및 성능 차트 생성

5단계: 실시간 추론

```bash
python 5_inference_demo.py
```

- 웹캠 실시간 손동작 인식
- 이미지/비디오 파일 처리
- 배치 처리 기능
