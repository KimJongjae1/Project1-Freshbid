package FreshBid.back.controller.impl;

import FreshBid.back.controller.CategoryController;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.product.CategoryCreateRequestDto;
import FreshBid.back.dto.product.CategoryResponseDto;
import FreshBid.back.dto.product.CategoryUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<CommonResponse<List<CategoryResponseDto>>> getAllCategories() {
        log.info("[카테고리 전체 조회 요청]");
        List<CategoryResponseDto> result = categoryService.getAllCategories();
        CommonResponse<List<CategoryResponseDto>> response = CommonResponse.<List<CategoryResponseDto>>builder()
                .success(true)
                .message("카테고리 전체 조회 성공")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> createCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            CategoryCreateRequestDto dto
    ) {
        log.info("[카테고리 등록 요청] 요청자: {}", userDetails.getUsername());
        categoryService.createCategory(dto);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("카테고리 등록 성공")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> updateCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Integer id,
            CategoryUpdateRequestDto dto
    ) {
        log.info("[카테고리 수정 요청] 요청자: {}, 수정 ID: {}", userDetails.getUsername(), id);
        categoryService.updateCategory(id, dto);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("카테고리 수정 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Integer id
    ) {
        log.info("[카테고리 삭제 요청] 요청자: {}, 삭제 ID: {}", userDetails.getUsername(), id);
        categoryService.deleteCategory(id);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("카테고리 삭제 성공")
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<CategoryResponseDto>> getCategoryByName(String name) {
        log.info("[카테고리 이름 검색] name: {}", name);
        CategoryResponseDto result = categoryService.getCategoryByName(name);

        CommonResponse<CategoryResponseDto> response = CommonResponse.<CategoryResponseDto>builder()
                .success(true)
                .message("카테고리 이름 검색 성공")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<List<CategoryResponseDto>>> getCategoriesBySuperId(Integer superId) {
        log.info("[상위 카테고리 기준 하위 목록 조회] superId: {}", superId);
        List<CategoryResponseDto> result = categoryService.getCategoriesBySuperId(superId);

        CommonResponse<List<CategoryResponseDto>> response = CommonResponse.<List<CategoryResponseDto>>builder()
                .success(true)
                .message("하위 카테고리 목록 조회 성공")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }
}
