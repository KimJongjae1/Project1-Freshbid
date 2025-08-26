package FreshBid.back.service.impl;

import FreshBid.back.dto.price.PriceDataRequestDto;
import FreshBid.back.service.CategoryCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCacheServiceImpl implements CategoryCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SUPER_CATEGORIES_KEY = "category:super";
    private static final String SUB_CATEGORIES_PREFIX = "category:sub:";
    private static final String GRADES_PREFIX = "category:grades:";
    private static final Duration CACHE_TTL = Duration.ofDays(7); // 1주일

    @Override
    public void cacheSuperCategories(List<PriceDataRequestDto> superCategories) {
        try {
            String json = objectMapper.writeValueAsString(superCategories);
            redisTemplate.opsForValue().set(SUPER_CATEGORIES_KEY, json, CACHE_TTL);
            log.info("상위 카테고리 캐싱 완료 - {}개 항목", superCategories.size());
        } catch (JsonProcessingException e) {
            log.error("상위 카테고리 캐싱 실패", e);
        }
    }

    @Override
    public List<PriceDataRequestDto> getCachedSuperCategories() {
        try {
            String json = redisTemplate.opsForValue().get(SUPER_CATEGORIES_KEY);
            if (json != null) {
                List<PriceDataRequestDto> categories = objectMapper.readValue(json, 
                    new TypeReference<List<PriceDataRequestDto>>() {});
                log.debug("Redis에서 상위 카테고리 조회 - {}개 항목", categories.size());
                return categories;
            }
        } catch (JsonProcessingException e) {
            log.error("상위 카테고리 캐시 조회 실패", e);
        }
        return null;
    }

    @Override
    public void cacheSubCategories(Long superCategoryId, List<PriceDataRequestDto> subCategories) {
        try {
            String key = SUB_CATEGORIES_PREFIX + superCategoryId;
            String json = objectMapper.writeValueAsString(subCategories);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL);
            log.info("하위 카테고리 캐싱 완료 - 상위카테고리ID: {}, {}개 항목", superCategoryId, subCategories.size());
        } catch (JsonProcessingException e) {
            log.error("하위 카테고리 캐싱 실패 - 상위카테고리ID: {}", superCategoryId, e);
        }
    }

    @Override
    public List<PriceDataRequestDto> getCachedSubCategories(Long superCategoryId) {
        try {
            String key = SUB_CATEGORIES_PREFIX + superCategoryId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                List<PriceDataRequestDto> categories = objectMapper.readValue(json, 
                    new TypeReference<List<PriceDataRequestDto>>() {});
                log.debug("Redis에서 하위 카테고리 조회 - 상위카테고리ID: {}, {}개 항목", superCategoryId, categories.size());
                return categories;
            }
        } catch (JsonProcessingException e) {
            log.error("하위 카테고리 캐시 조회 실패 - 상위카테고리ID: {}", superCategoryId, e);
        }
        return null;
    }

    @Override
    public void cacheGrades(Long categoryId, List<String> grades) {
        try {
            String key = GRADES_PREFIX + categoryId;
            String json = objectMapper.writeValueAsString(grades);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL);
            log.info("등급 캐싱 완료 - 카테고리ID: {}, {}개 항목", categoryId, grades.size());
        } catch (JsonProcessingException e) {
            log.error("등급 캐싱 실패 - 카테고리ID: {}", categoryId, e);
        }
    }

    @Override
    public List<String> getCachedGrades(Long categoryId) {
        try {
            String key = GRADES_PREFIX + categoryId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                List<String> grades = objectMapper.readValue(json, 
                    new TypeReference<List<String>>() {});
                log.debug("Redis에서 등급 조회 - 카테고리ID: {}, {}개 항목", categoryId, grades.size());
                return grades;
            }
        } catch (JsonProcessingException e) {
            log.error("등급 캐시 조회 실패 - 카테고리ID: {}", categoryId, e);
        }
        return null;
    }

    @Override
    public void initializeCategoryCache() {
        log.info("카테고리 캐시 초기화 시작");
        // 이 메서드는 애플리케이션 시작 시 또는 관리자 요청 시 호출됩니다.
        // 실제 구현에서는 PriceDataService를 주입받아 모든 카테고리 데이터를 캐싱합니다.
    }

    @Override
    public void clearCategoryCache(Long categoryId) {
        String subKey = SUB_CATEGORIES_PREFIX + categoryId;
        String gradesKey = GRADES_PREFIX + categoryId;
        
        redisTemplate.delete(subKey);
        redisTemplate.delete(gradesKey);
        
        log.info("카테고리 캐시 삭제 완료 - 카테고리ID: {}", categoryId);
    }

    @Override
    public void clearAllCategoryCache() {
        redisTemplate.delete(SUPER_CATEGORIES_KEY);
        
        // 패턴으로 모든 하위 카테고리와 등급 캐시 삭제
        Set<String> subKeys = redisTemplate.keys(SUB_CATEGORIES_PREFIX + "*");
        Set<String> gradesKeys = redisTemplate.keys(GRADES_PREFIX + "*");
        
        if (subKeys != null && !subKeys.isEmpty()) {
            redisTemplate.delete(subKeys);
        }
        if (gradesKeys != null && !gradesKeys.isEmpty()) {
            redisTemplate.delete(gradesKeys);
        }
        
        log.info("모든 카테고리 캐시 삭제 완료");
    }
}
