# 농산품 신선도 탐지 API

## 설치 및 실행 가이드

### 1. Python 환경 설정
```bash
# Python 3.10 설치
conda install python=3.10
```

### 2. Conda 패키지 설치
```bash
# conda install.txt 내의 명령어 실행
conda install -c conda-forge tqdm -y
conda install -c conda-forge matplotlib -y
conda install -c conda-forge pandas -y
conda install -c conda-forge opencv -y
```

### 3. pip 패키지 설치
```bash
# requirements.txt에서 추가 패키지 설치
pip install -r requirements.txt
```

### 4. API 실행
```bash
# API 서버 실행
python api.py
```

## 포함된 주요 패키지
- **딥러닝**: TensorFlow 2.11.0, PyTorch 2.7.1+cu128, Keras 2.11.0
- **웹 프레임워크**: Flask 2.2.3, Flask-CORS 3.0.10
- **컴퓨터 비전**: OpenCV 4.12.0, Pillow 9.4.0
- **데이터 처리**: NumPy 1.24.3, Pandas, Matplotlib 3.10.3
- **기타**: TensorBoard, Rich, PySide6