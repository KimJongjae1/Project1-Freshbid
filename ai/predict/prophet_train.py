import os
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
import platform
from prophet import Prophet

# ✅ 한글 폰트 설정
if platform.system() == 'Windows':
    plt.rc('font', family='Malgun Gothic')
else:
    plt.rc('font', family='AppleGothic')
plt.rcParams['axes.unicode_minus'] = False  # 마이너스 기호 깨짐 방지

# 📁 폴더 설정
RESULT_DIR = "results"
PLOT_DIR = "plots"
FORECAST_DIR = "forecast"
os.makedirs(PLOT_DIR, exist_ok=True)
os.makedirs(FORECAST_DIR, exist_ok=True)

# 🔁 모든 CSV 순회
for file in os.listdir(RESULT_DIR):
    if not file.endswith(".csv"):
        continue

    name = file.replace(".csv", "")
    parts = name.split("_")
    if len(parts) != 2:
        print(f"⚠️ 잘못된 파일명 형식: {file}")
        continue

    item, grade = parts
    filepath = os.path.join(RESULT_DIR, file)

    try:
        # 📊 데이터 로드 및 컬럼 정리
        df = pd.read_csv(filepath)
        df = df.rename(columns={"date": "ds", "price_per_kg": "y"})
        df["ds"] = pd.to_datetime(df["ds"])

        if len(df) < 2:
            print(f"⛔ {file} → 데이터가 너무 적음")
            continue

        # 🔁 Prophet 학습용 복사본
        df_for_model = df[["ds", "y"]].copy()

        # 📉 시각화용: 이동평균 처리
        df["y_smooth"] = df["y"].rolling(window=7, center=True).mean()

        # 🤖 Prophet 모델 학습
        model = Prophet(yearly_seasonality=True, weekly_seasonality=False, daily_seasonality=False)
        model.fit(df_for_model)

        # 🔮 미래 예측 (90일)
        future = model.make_future_dataframe(periods=90)
        forecast = model.predict(future)

        # 🎨 그래프 생성
        plt.figure(figsize=(12, 6))
        plt.plot(df["ds"], df["y_smooth"], label="실제 (7일 평균)", linewidth=2)
        plt.plot(forecast["ds"], forecast["yhat"], linestyle="--", label="예측", linewidth=2, color="orange")

        plt.title(f"{item} ({grade}) 가격 추이")
        plt.xlabel("날짜")
        plt.ylabel("가격 (원/kg)")
        plt.legend()
        plt.grid(True)
        plt.tight_layout()

        # 💾 그래프 저장
        save_path = os.path.join(PLOT_DIR, f"{item}_{grade}_graph.png")
        plt.savefig(save_path)
        plt.close()
        print(f"✅ 그래프 저장 완료: {save_path}")

        # 💾 예측 CSV 저장
        forecast_save_path = os.path.join(FORECAST_DIR, f"{item}_{grade}_forecast.csv")
        forecast[["ds", "yhat"]].to_csv(forecast_save_path, index=False)
        print(f"📈 예측 CSV 저장: {forecast_save_path}")

    except Exception as e:
        print(f"❌ {file} 처리 중 오류 발생: {e}")
