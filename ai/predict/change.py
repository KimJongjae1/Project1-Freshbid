import os
import pandas as pd

# 📁 경로 설정
FORECAST_DIR = "forecast"
TS_DIR = "forecastTS"
os.makedirs(TS_DIR, exist_ok=True)

# 🔁 모든 forecast CSV 순회
for file in os.listdir(FORECAST_DIR):
    if not file.endswith(".csv"):
        continue

    filepath = os.path.join(FORECAST_DIR, file)
    name = os.path.splitext(file)[0]
    ts_filename = f"{name}.ts"
    ts_filepath = os.path.join(TS_DIR, ts_filename)

    try:
        df = pd.read_csv(filepath)

        # 📦 TypeScript 배열 생성
        lines = ['const forecastData = [']
        for _, row in df.iterrows():
            ds = row['ds']
            yhat = row['yhat']
            lines.append(f'  {{ ds: "{ds}", yhat: {yhat:.2f} }},')
        lines.append('];\n\nexport default forecastData;')

        # 💾 저장
        with open(ts_filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))

        print(f"✅ 변환 완료: {ts_filepath}")

    except Exception as e:
        print(f"❌ {file} 변환 중 오류: {e}")
