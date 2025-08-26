import os
import pandas as pd
import re
from pathlib import Path

# 입력/출력 경로
DATA_DIR = 'data'
OUTPUT_DIR = 'results'
os.makedirs(OUTPUT_DIR, exist_ok=True)

def extract_kg_from_unit(unit_str):
    """'10키로상자' → 10 추출"""
    try:
        numbers = re.findall(r'\d+', str(unit_str))
        return int(numbers[0]) if numbers else 1
    except:
        return 1

def preprocess_file(filepath):
    # 품목명 추출
    item_name = os.path.splitext(os.path.basename(filepath))[0].replace('특상품중하', '').replace('상품중하', '').replace('특상중하', '')

    # 파일 로드
    df = pd.read_excel(filepath)

    # 필수 컬럼 확인
    required_cols = ['DATE', '품목명', '단위', '등급명', '평균가격']
    if not all(col in df.columns for col in required_cols):
        print(f"[SKIP] {filepath}: 필수 컬럼 없음")
        return

    # 전처리
    df = df[required_cols].copy()
    df.dropna(subset=['평균가격'], inplace=True)
    
    # 쉼표 제거 및 숫자로 변환
    df['평균가격'] = df['평균가격'].astype(str).str.replace(',', '')
    df['평균가격'] = pd.to_numeric(df['평균가격'], errors='coerce')

    # 단위 중 kg 수 추출
    df['단위kg'] = df['단위'].apply(extract_kg_from_unit)
    
    # 1kg당 가격 계산 (0 이상만)
    df['가격(kg당)'] = (df['평균가격'] / df['단위kg']).round().astype('Int64')
    df = df[df['가격(kg당)'] > 0]

    # 필요한 열만 추출
    df_final = df[['DATE', '품목명', '등급명', '가격(kg당)']]
    df_final.columns = ['date', 'item', 'grade', 'price_per_kg']

    # 등급별로 분리 저장
    for grade, sub_df in df_final.groupby('grade'):
        save_path = os.path.join(OUTPUT_DIR, f'{item_name}_{grade}.csv')
        sub_df.to_csv(save_path, index=False, encoding='utf-8-sig')
        print(f"[OK] 저장: {save_path}")

def main():
    for file in os.listdir(DATA_DIR):
        if file.endswith('.xlsx'):
            preprocess_file(os.path.join(DATA_DIR, file))

if __name__ == '__main__':
    main()
