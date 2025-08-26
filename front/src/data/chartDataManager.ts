// 차트 데이터 관리 시스템
export interface ChartData {
  item: string;
  grade: string;
  actual: {
    dates: string[];
    prices: number[];
  };
  prediction: {
    dates: string[];
    prices: number[];
  };
  last_actual_date: string;
  last_actual_price: number;
}

export interface ForecastData {
  ds: string;
  yhat: number;
}

export interface ItemSummary {
  item: string;
  grade: string;
  latest_price: number;
  price_change: number;
  last_date: string;
  data_points: number;
  min_price: number;
  max_price: number;
}

// 카테고리 ID와 등급으로 직접 데이터 로드 (주요 함수)
export const loadChartDataById = async (categoryId: number, grade: string): Promise<ChartData | null> => {
  try {
    // 백엔드 API 호출
    const axiosInstance = await import('../api/axiosInstance').then(m => m.default);
    const response = await axiosInstance.get(`/price/chart/${categoryId}/${grade}`);
    const data = response.data;
    
    // 백엔드 응답을 ChartData 형식으로 변환
    console.log(`loadChartDataById - 실제 데이터: ${data.data.actualData.length}개, 예측 데이터: ${data.data.forecastData.length}개`);
    
    // 3년 전부터 오늘까지의 실제 데이터 필터링
    const threeYearsAgo = new Date();
    threeYearsAgo.setFullYear(threeYearsAgo.getFullYear() - 3);
    const today = new Date();
    
    const filteredActualData = data.data.actualData.filter((point: any) => {
      const pointDate = new Date(point.date);
      return pointDate >= threeYearsAgo && pointDate <= today;
    });
    
    // 3년 전부터 90일 후까지의 예측 데이터 필터링
    const forecastEndDate = new Date();
    forecastEndDate.setDate(forecastEndDate.getDate() + 90);
    
    const filteredForecastData = data.data.forecastData.filter((point: any) => {
      const pointDate = new Date(point.date);
      return pointDate >= threeYearsAgo && pointDate <= forecastEndDate;
    });
    
    console.log(`필터링 후 - 실제 데이터: ${filteredActualData.length}개, 예측 데이터: ${filteredForecastData.length}개`);
    
    return {
      item: data.data.itemName,
      grade: data.data.grade,
      actual: {
        dates: filteredActualData.map((point: any) => point.date),
        prices: filteredActualData.map((point: any) => point.price)
      },
      prediction: {
        dates: filteredForecastData.map((point: any) => point.date),
        prices: filteredForecastData.map((point: any) => point.price)
      },
      last_actual_date: data.data.lastUpdate,
      last_actual_price: data.data.currentPrice
    };
  } catch (error) {
    console.error(`차트 데이터 로드 실패: categoryId=${categoryId}, grade=${grade}`, error);
    return null;
  }
};

// 상위 카테고리 목록 가져오기
export const getSuperCategories = async (): Promise<any[]> => {
  try {
    const axiosInstance = await import('../api/axiosInstance').then(m => m.default);
    const response = await axiosInstance.get('/price/super-categories');
    return response.data.data || [];
  } catch (error) {
    console.error('상위 카테고리 로드 실패:', error);
    return [];
  }
};

// 하위 카테고리 목록 가져오기
export const getSubCategories = async (superCategoryId: number): Promise<any[]> => {
  try {
    const axiosInstance = await import('../api/axiosInstance').then(m => m.default);
    const response = await axiosInstance.get(`/price/sub-categories/${superCategoryId}`);
    return response.data.data || [];
  } catch (error) {
    console.error('하위 카테고리 로드 실패:', error);
    return [];
  }
};
