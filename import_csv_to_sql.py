import os
import pandas as pd
import mysql.connector
from mysql.connector import Error
import glob
import re

# 데이터베이스 연결 설정
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'ssafy',  # 비밀번호 설정
    'database': 'freshbid',
    'charset': 'utf8mb4'
}

def connect_to_database():
    """데이터베이스에 연결"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print("MySQL 데이터베이스에 성공적으로 연결되었습니다.")
            return connection
    except Error as e:
        print(f"데이터베이스 연결 오류: {e}")
        return None

def get_category_id_by_name(connection, category_name):
    """카테고리 이름으로 ID를 조회"""
    try:
        cursor = connection.cursor()
        query = "SELECT id FROM product_category WHERE name = %s"
        cursor.execute(query, (category_name,))
        result = cursor.fetchone()
        cursor.close()
        return result[0] if result else None
    except Error as e:
        print(f"카테고리 ID 조회 오류 ({category_name}): {e}")
        return None

def import_results_data(connection, results_dir):
    """실제 가격 데이터(results)를 price_observation 테이블에 주입"""
    print("\n=== 실제 가격 데이터 주입 시작 ===")
    
    # results_utf8 폴더의 모든 CSV 파일 찾기
    csv_files = glob.glob(os.path.join(results_dir, "*.csv"))
    
    if not csv_files:
        print("CSV 파일을 찾을 수 없습니다.")
        return
    
    print(f"총 {len(csv_files)}개의 CSV 파일을 처리합니다.")
    
    for csv_file in csv_files:
        try:
            # 파일명에서 카테고리명과 등급 추출 (예: "토마토_상.csv" -> "토마토", "상")
            filename = os.path.basename(csv_file)
            match = re.match(r'(.+)_(.+)\.csv', filename)
            if not match:
                print(f"파일명 형식 오류: {filename}")
                continue
                
            category_name, grade = match.groups()
            print(f"처리 중: {category_name}_{grade}")
            
            # 카테고리 ID 조회
            category_id = get_category_id_by_name(connection, category_name)
            if not category_id:
                print(f"카테고리를 찾을 수 없음: {category_name}")
                continue
            
            # CSV 파일 읽기
            df = pd.read_csv(csv_file)
            
            # 데이터 삽입
            cursor = connection.cursor()
            
            # 기존 데이터 삭제 (같은 카테고리, 등급의 데이터)
            delete_query = """
            DELETE FROM price_observation 
            WHERE item_category_id = %s AND grade = %s
            """
            cursor.execute(delete_query, (category_id, grade))
            
            # 새 데이터 삽입 (중복 키 무시)
            insert_query = """
            INSERT IGNORE INTO price_observation 
            (source, item_category_id, grade, observed_at, price_per_kg, created_at)
            VALUES (%s, %s, %s, %s, %s, NOW())
            """
            
            inserted_count = 0
            for _, row in df.iterrows():
                try:
                    cursor.execute(insert_query, (
                        'EXTERNAL',  # source
                        category_id,  # item_category_id
                        grade,  # grade
                        row['date'],  # observed_at
                        row['price_per_kg']  # price_per_kg
                    ))
                    inserted_count += 1
                except Error as e:
                    print(f"데이터 삽입 오류 ({row['date']}): {e}")
                    continue
            
            connection.commit()
            cursor.close()
            print(f"  완료: {inserted_count}개 데이터 삽입")
            
        except Exception as e:
            print(f"파일 처리 오류 ({csv_file}): {e}")
            continue

def import_forecast_data(connection, forecast_dir):
    """예측 데이터(forecast)를 price_forecast 테이블에 주입"""
    print("\n=== 예측 데이터 주입 시작 ===")
    
    # forecast_utf8 폴더의 모든 CSV 파일 찾기
    csv_files = glob.glob(os.path.join(forecast_dir, "*_forecast.csv"))
    
    if not csv_files:
        print("예측 CSV 파일을 찾을 수 없습니다.")
        return
    
    print(f"총 {len(csv_files)}개의 예측 CSV 파일을 처리합니다.")
    
    for csv_file in csv_files:
        try:
            # 파일명에서 카테고리명과 등급 추출 (예: "토마토_상_forecast.csv" -> "토마토", "상")
            filename = os.path.basename(csv_file)
            match = re.match(r'(.+)_(.+)_forecast\.csv', filename)
            if not match:
                print(f"파일명 형식 오류: {filename}")
                continue
                
            category_name, grade = match.groups()
            print(f"처리 중: {category_name}_{grade}_forecast")
            
            # 카테고리 ID 조회
            category_id = get_category_id_by_name(connection, category_name)
            if not category_id:
                print(f"카테고리를 찾을 수 없음: {category_name}")
                continue
            
            # CSV 파일 읽기
            df = pd.read_csv(csv_file)
            
            # 데이터 삽입
            cursor = connection.cursor()
            
            # 기존 데이터 삭제 (같은 카테고리, 등급의 데이터)
            delete_query = """
            DELETE FROM price_forecast 
            WHERE item_category_id = %s AND grade = %s
            """
            cursor.execute(delete_query, (category_id, grade))
            
            # 새 데이터 삽입 (중복 키 무시)
            insert_query = """
            INSERT IGNORE INTO price_forecast 
            (item_category_id, grade, ds, yhat, created_at)
            VALUES (%s, %s, %s, %s, NOW())
            """
            
            inserted_count = 0
            for _, row in df.iterrows():
                try:
                    cursor.execute(insert_query, (
                        category_id,  # item_category_id
                        grade,  # grade
                        row['ds'],  # ds (date)
                        row['yhat']  # yhat (predicted price)
                    ))
                    inserted_count += 1
                except Error as e:
                    print(f"예측 데이터 삽입 오류 ({row['ds']}): {e}")
                    continue
            
            connection.commit()
            cursor.close()
            print(f"  완료: {inserted_count}개 예측 데이터 삽입")
            
        except Exception as e:
            print(f"예측 파일 처리 오류 ({csv_file}): {e}")
            continue

def main():
    """메인 함수"""
    print("CSV 데이터를 SQL에 주입하는 스크립트를 시작합니다.")
    
    # 데이터베이스 연결
    connection = connect_to_database()
    if not connection:
        return
    
    try:
        # 실제 가격 데이터 주입
        results_dir = "ai/predict/results/results_utf8"
        if os.path.exists(results_dir):
            import_results_data(connection, results_dir)
        else:
            print(f"결과 데이터 폴더를 찾을 수 없습니다: {results_dir}")
        
        # 예측 데이터 주입
        forecast_dir = "ai/predict/forecast/forecast_utf8"
        if os.path.exists(forecast_dir):
            import_forecast_data(connection, forecast_dir)
        else:
            print(f"예측 데이터 폴더를 찾을 수 없습니다: {forecast_dir}")
        
        print("\n=== 모든 데이터 주입 완료 ===")
        
    except Exception as e:
        print(f"오류 발생: {e}")
    finally:
        if connection.is_connected():
            connection.close()
            print("데이터베이스 연결이 종료되었습니다.")

if __name__ == "__main__":
    main()
