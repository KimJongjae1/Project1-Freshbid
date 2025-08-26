import torch
from ultralytics import YOLO

# PyTorch 모델 로드
model = YOLO("./best.pt")

# ONNX 변환 시 올바른 파라미터 설정
model.export(
    format="onnx",
    imgsz=640,
    dynamic=False,  # 동적 배치 사이즈 비활성화
    simplify=True,  # 모델 단순화
    opset=11,       # ONNX opset 버전 명시
)