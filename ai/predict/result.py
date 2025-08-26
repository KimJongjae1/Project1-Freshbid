import pandas as pd
import matplotlib.pyplot as plt
import os

# 폴더 설정
result_dir = "results"
plot_dir = "plots"
os.makedirs(plot_dir, exist_ok=True)

# 모든 forecast 파일에 대해 반복
for file in os.listdir(result_dir):
    if not file.endswith("_forecast.csv"):
        continue

    base = file.replace("_forecast.csv", "")
    forecast_path = os.path.join(result_dir, f"{base}_forecast.csv")
    actual_path = os.path.join(result_dir, f"{base}_actual.csv")

    # 파일 로드
    forecast = pd.read_csv(forecast_path, parse_dates=["ds"])
    actual = pd.read_csv(actual_path, parse_dates=["ds"])

    # 실제 학습 범위까지
    last_train_date = actual["ds"].max()
    forecast_future = forecast[forecast["ds"] > last_train_date]

    # 그래프 그리기
    plt.figure(figsize=(12, 6))
    plt.plot(actual["ds"], actual["y"], label="실제가격", color="green", linewidth=2)
    plt.plot(forecast_future["ds"], forecast_future["yhat"], label="예측가격", color="blue", linewidth=2)
    plt.fill_between(forecast_future["ds"], forecast_future["yhat_lower"], forecast_future["yhat_upper"], 
                     color="blue", alpha=0.2, label="예측범위")

    plt.title(f"{base} 가격 예측", fontsize=14)
    plt.xlabel("날짜"); plt.ylabel("원/kg")
    plt.legend()
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.tight_layout()

    # 저장
    plt.savefig(f"{plot_dir}/{base}.png")
    plt.close()

    print(f"✅ {base} 그래프 저장 완료")
