from flask import Flask, jsonify, request
from flask_cors import CORS
import pandas as pd
import os
import json
from prophet import Prophet
import numpy as np

app = Flask(__name__)
CORS(app)

# 결과 디렉토리 설정
RESULTS_DIR = "results"
PLOTS_DIR = "plots"

@app.route('/api/predictions/items', methods=['GET'])
def get_available_items():
    """사용 가능한 예측 아이템 목록을 반환합니다."""
    items = []
    
    if os.path.exists(RESULTS_DIR):
        for file in os.listdir(RESULTS_DIR):
            if file.endswith('.csv'):
                name = file.replace('.csv', '')
                parts = name.split('_')
                if len(parts) == 2:
                    item, grade = parts
                    items.append({
                        'item': item,
                        'grade': grade,
                        'filename': file
                    })
    
    return jsonify({'items': items})

@app.route('/api/predictions/<item>/<grade>', methods=['GET'])
def get_prediction_data(item, grade):
    """특정 아이템의 예측 데이터를 반환합니다."""
    try:
        filename = f"{item}_{grade}.csv"
        filepath = os.path.join(RESULTS_DIR, filename)
        
        if not os.path.exists(filepath):
            return jsonify({'error': '데이터를 찾을 수 없습니다.'}), 404
        
        # CSV 파일 읽기
        df = pd.read_csv(filepath)
        df['date'] = pd.to_datetime(df['date'])
        
        # Prophet 모델 학습
        df_for_model = df[['date', 'price_per_kg']].copy()
        df_for_model.columns = ['ds', 'y']
        
        model = Prophet(
            yearly_seasonality=True,
            weekly_seasonality=False,
            daily_seasonality=False,
            interval_width=0.95
        )
        model.fit(df_for_model)
        
        # 미래 예측 (365일)
        future = model.make_future_dataframe(periods=365)
        forecast = model.predict(future)
        
        # 실제 데이터와 예측 데이터 분리
        actual_data = df_for_model[df_for_model['ds'] <= df_for_model['ds'].max()]
        future_data = forecast[forecast['ds'] > df_for_model['ds'].max()]
        
        # 응답 데이터 구성
        response_data = {
            'item': item,
            'grade': grade,
            'actual': {
                'dates': actual_data['ds'].dt.strftime('%Y-%m-%d').tolist(),
                'prices': actual_data['y'].tolist()
            },
            'prediction': {
                'dates': future_data['ds'].dt.strftime('%Y-%m-%d').tolist(),
                'prices': future_data['yhat'].round(2).tolist(),
                'lower_bound': future_data['yhat_lower'].round(2).tolist(),
                'upper_bound': future_data['yhat_upper'].round(2).tolist()
            },
            'last_actual_date': actual_data['ds'].max().strftime('%Y-%m-%d'),
            'last_actual_price': float(actual_data['y'].iloc[-1])
        }
        
        return jsonify(response_data)
        
    except Exception as e:
        return jsonify({'error': f'예측 데이터 생성 중 오류가 발생했습니다: {str(e)}'}), 500

@app.route('/api/predictions/summary', methods=['GET'])
def get_prediction_summary():
    """모든 아이템의 예측 요약 정보를 반환합니다."""
    try:
        summary = []
        
        if os.path.exists(RESULTS_DIR):
            for file in os.listdir(RESULTS_DIR):
                if file.endswith('.csv'):
                    name = file.replace('.csv', '')
                    parts = name.split('_')
                    if len(parts) == 2:
                        item, grade = parts
                        filepath = os.path.join(RESULTS_DIR, file)
                        
                        df = pd.read_csv(filepath)
                        df['date'] = pd.to_datetime(df['date'])
                        
                        # 최근 가격 정보
                        latest_price = df['price_per_kg'].iloc[-1]
                        price_change = df['price_per_kg'].iloc[-1] - df['price_per_kg'].iloc[-2] if len(df) > 1 else 0
                        
                        summary.append({
                            'item': item,
                            'grade': grade,
                            'latest_price': float(latest_price),
                            'price_change': float(price_change),
                            'last_date': df['date'].max().strftime('%Y-%m-%d'),
                            'data_points': len(df)
                        })
        
        return jsonify({'summary': summary})
        
    except Exception as e:
        return jsonify({'error': f'요약 정보 생성 중 오류가 발생했습니다: {str(e)}'}), 500

@app.route('/api/predictions/generate', methods=['POST'])
def generate_prediction():
    """새로운 데이터로 예측을 생성합니다."""
    try:
        data = request.get_json()
        category_id = data.get('categoryId')
        grade = data.get('grade')
        price_data = data.get('data')  # [{"date": "2024-01-01", "price": 1000}, ...]
        
        if not price_data or len(price_data) < 10:
            return jsonify({'error': '예측을 위한 데이터가 부족합니다. 최소 10개 이상의 데이터가 필요합니다.'}), 400
        
        # 데이터를 DataFrame으로 변환
        df = pd.DataFrame(price_data)
        df['date'] = pd.to_datetime(df['date'])
        df = df.rename(columns={'price': 'price_per_kg'})
        
        # Prophet 모델 학습
        df_for_model = df[['date', 'price_per_kg']].copy()
        df_for_model.columns = ['ds', 'y']
        
        model = Prophet(
            yearly_seasonality=True,
            weekly_seasonality=False,
            daily_seasonality=False,
            interval_width=0.95
        )
        model.fit(df_for_model)
        
        # 미래 예측 (1년)
        future = model.make_future_dataframe(periods=365)
        forecast = model.predict(future)
        
        # 예측 데이터만 추출 (현재 날짜 이후)
        future_data = forecast[forecast['ds'] > df_for_model['ds'].max()]
        
        # 응답 데이터 구성
        response_data = {
            'categoryId': category_id,
            'grade': grade,
            'forecast': [
                {
                    'date': row['ds'].strftime('%Y-%m-%d'),
                    'price': round(float(row['yhat']), 2),
                    'lower_bound': round(float(row['yhat_lower']), 2),
                    'upper_bound': round(float(row['yhat_upper']), 2)
                }
                for _, row in future_data.iterrows()
            ]
        }
        
        return jsonify(response_data)
        
    except Exception as e:
        return jsonify({'error': f'예측 생성 중 오류가 발생했습니다: {str(e)}'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
