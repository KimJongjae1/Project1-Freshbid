package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.product.CategoryCreateRequestDto;
import FreshBid.back.dto.product.CategoryUpdateRequestDto;
import FreshBid.back.dto.product.CategoryResponseDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category 관리 API", description = "카테고리 등록, 조회, 수정, 삭제 관련 API")
@RequestMapping("/categories")
public interface CategoryController {

    @Operation(summary = "전체 카테고리 조회", description = "등록된 전체 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("")
    ResponseEntity<CommonResponse<List<CategoryResponseDto>>> getAllCategories();

    @Operation(summary = "카테고리 등록", description = "새로운 카테고리를 등록합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카테고리 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    ResponseEntity<CommonResponse<Void>> createCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @RequestBody CategoryCreateRequestDto dto
    );

    @Operation(summary = "카테고리 수정", description = "기존 카테고리 정보를 수정합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{id}")
    ResponseEntity<CommonResponse<Void>> updateCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Integer id,
            @RequestBody CategoryUpdateRequestDto dto
    );

    @Operation(summary = "카테고리 삭제", description = "기존 카테고리를 삭제합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<CommonResponse<Void>> deleteCategory(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Integer id
    );

    @Operation(summary = "이름으로 카테고리 검색", description = "카테고리 이름으로 단건 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 검색 성공"),
            @ApiResponse(responseCode = "404", description = "해당 이름의 카테고리 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/search")
    ResponseEntity<CommonResponse<CategoryResponseDto>> getCategoryByName(@RequestParam String name);

    @Operation(summary = "상위 카테고리로 하위 카테고리 조회", description = "상위 카테고리 ID를 통해 하위 카테고리들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위 카테고리 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/super/{superId}")
    ResponseEntity<CommonResponse<List<CategoryResponseDto>>> getCategoriesBySuperId(@PathVariable Integer superId);
}

