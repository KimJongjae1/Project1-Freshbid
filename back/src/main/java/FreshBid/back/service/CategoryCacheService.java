package FreshBid.back.service;

import FreshBid.back.dto.price.PriceDataRequestDto;

import java.util.List;

public interface CategoryCacheService {
    
    /**
     * 상위 카테고리 목록을 Redis에 캐싱 (1주일 TTL)
     */
    void cacheSuperCategories(List<PriceDataRequestDto> superCategories);
    
    /**
     * Redis에서 상위 카테고리 목록 조회
     */
    List<PriceDataRequestDto> getCachedSuperCategories();
    
    /**
     * 하위 카테고리 목록을 Redis에 캐싱 (1주일 TTL)
     */
    void cacheSubCategories(Long superCategoryId, List<PriceDataRequestDto> subCategories);
    
    /**
     * Redis에서 하위 카테고리 목록 조회
     */
    List<PriceDataRequestDto> getCachedSubCategories(Long superCategoryId);
    
    /**
     * 등급 목록을 Redis에 캐싱 (1주일 TTL)
     */
    void cacheGrades(Long categoryId, List<String> grades);
    
    /**
     * Redis에서 등급 목록 조회
     */
    List<String> getCachedGrades(Long categoryId);
    
    /**
     * 모든 카테고리 데이터를 Redis에 초기 캐싱
     */
    void initializeCategoryCache();
    
    /**
     * 특정 카테고리 캐시 삭제
     */
    void clearCategoryCache(Long categoryId);
    
    /**
     * 모든 카테고리 캐시 삭제
     */
    void clearAllCategoryCache();
}
