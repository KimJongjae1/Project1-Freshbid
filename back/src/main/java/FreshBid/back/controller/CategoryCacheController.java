package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.service.CategoryCacheService;
import FreshBid.back.service.PriceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/category-cache")
@RequiredArgsConstructor
@Tag(name = "Category Cache", description = "카테고리 캐시 관리 API")
public class CategoryCacheController {

    private final CategoryCacheService categoryCacheService;
    private final PriceDataService priceDataService;

    @PostMapping("/initialize")
    @Operation(summary = "카테고리 캐시 초기화", description = "모든 카테고리 데이터를 Redis에 캐싱합니다.")
    public ResponseEntity<CommonResponse<String>> initializeCategoryCache() {
        try {
            log.info("카테고리 캐시 초기화 시작");
            
            // 상위 카테고리 캐싱
            var superCategories = priceDataService.getSuperCategories();
            categoryCacheService.cacheSuperCategories(superCategories);
            
            // 각 상위 카테고리의 하위 카테고리와 등급 캐싱
            for (var superCategory : superCategories) {
                var subCategories = priceDataService.getSubCategories(superCategory.getCategoryId());
                categoryCacheService.cacheSubCategories(superCategory.getCategoryId(), subCategories);
                
                for (var subCategory : subCategories) {
                    var grades = priceDataService.getAvailableGrades(subCategory.getCategoryId());
                    categoryCacheService.cacheGrades(subCategory.getCategoryId(), grades);
                }
            }
            
            log.info("카테고리 캐시 초기화 완료");
            return ResponseEntity.ok(CommonResponse.<String>builder()
                    .success(true)
                    .message("카테고리 캐시가 성공적으로 초기화되었습니다.")
                    .data("success")
                    .build());
            
        } catch (Exception e) {
            log.error("카테고리 캐시 초기화 실패", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.<String>builder()
                            .success(false)
                            .message("카테고리 캐시 초기화 중 오류가 발생했습니다: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @DeleteMapping("/clear")
    @Operation(summary = "모든 카테고리 캐시 삭제", description = "Redis의 모든 카테고리 캐시를 삭제합니다.")
    public ResponseEntity<CommonResponse<String>> clearAllCategoryCache() {
        try {
            log.info("모든 카테고리 캐시 삭제 시작");
            categoryCacheService.clearAllCategoryCache();
            log.info("모든 카테고리 캐시 삭제 완료");
            return ResponseEntity.ok(CommonResponse.<String>builder()
                    .success(true)
                    .message("모든 카테고리 캐시가 삭제되었습니다.")
                    .data("success")
                    .build());
        } catch (Exception e) {
            log.error("카테고리 캐시 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.<String>builder()
                            .success(false)
                            .message("카테고리 캐시 삭제 중 오류가 발생했습니다: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @DeleteMapping("/clear/{categoryId}")
    @Operation(summary = "특정 카테고리 캐시 삭제", description = "특정 카테고리의 캐시를 삭제합니다.")
    public ResponseEntity<CommonResponse<String>> clearCategoryCache(@PathVariable Long categoryId) {
        try {
            log.info("카테고리 캐시 삭제 시작 - 카테고리ID: {}", categoryId);
            categoryCacheService.clearCategoryCache(categoryId);
            log.info("카테고리 캐시 삭제 완료 - 카테고리ID: {}", categoryId);
            return ResponseEntity.ok(CommonResponse.<String>builder()
                    .success(true)
                    .message("카테고리 캐시가 삭제되었습니다.")
                    .data("success")
                    .build());
        } catch (Exception e) {
            log.error("카테고리 캐시 삭제 실패 - 카테고리ID: {}", categoryId, e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.<String>builder()
                            .success(false)
                            .message("카테고리 캐시 삭제 중 오류가 발생했습니다: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }
}
