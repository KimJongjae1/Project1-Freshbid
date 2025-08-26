package FreshBid.back.service;

import FreshBid.back.dto.price.PriceChartResponseDto;
import FreshBid.back.dto.price.PriceDataRequestDto;
import FreshBid.back.entity.Order;

import java.util.List;

public interface PriceDataService {

    /**
     * 카테고리별 가격 차트 데이터 조회
     */
    PriceChartResponseDto getPriceChartData(Long categoryId, String grade);

    /**
     * 주문 완료 시 가격 데이터 추가
     */
    void addPriceDataFromOrder(Order order);

    /**
     * Prophet 모델로 예측 데이터 생성
     */
    void generateForecastData(Long categoryId, String grade);

    /**
     * 사용 가능한 카테고리 및 등급 목록 조회
     */
    List<PriceDataRequestDto> getAvailableCategories();
    List<PriceDataRequestDto> getSuperCategories();
    List<PriceDataRequestDto> getSubCategories(Long superCategoryId);
    
    /**
     * 카테고리별 사용 가능한 등급 목록 조회
     */
    List<String> getAvailableGrades(Long categoryId);
    List<PriceDataRequestDto> getAvailableCategoriesWithData();
    List<PriceDataRequestDto> getSuperCategoriesWithData();
    List<PriceDataRequestDto> getSubCategoriesWithData(Long superCategoryId);
}
