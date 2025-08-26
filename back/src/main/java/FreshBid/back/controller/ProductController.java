package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.product.ProductCreateRequestDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.product.ProductUpdateRequestDto;
import FreshBid.back.dto.product.ProductSearchRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 관리 API 인터페이스
 */
@Tag(name = "상품 관리 API", description = "상품(CRUD) 등록, 조회, 수정, 삭제 관련 API")
@RequestMapping("/auction/product")
public interface ProductController {

    @Operation(summary = "상품 등록", description = "신규 상품을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    ResponseEntity<CommonResponse<ProductResponseDto>> createProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @RequestBody ProductCreateRequestDto createDto
    );

    @Operation(summary = "상품 목록 조회", description = "검색 조건에 따라 상품 목록을 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("")
    ResponseEntity<CommonResponse<Page<ProductResponseDto>>> searchProducts(
            ProductSearchRequestDto searchRequest
    );

    @Operation(summary = "상품 단건 조회", description = "상품 ID로 단일 상품 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{productId}")
    ResponseEntity<CommonResponse<ProductResponseDto>> getProduct(
            @PathVariable("productId") Long productId
    );

    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 상품이 아닌 경우)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품 또는 카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{productId}")
    ResponseEntity<CommonResponse<Void>> updateProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @RequestBody ProductUpdateRequestDto updateDto
    );

    @Operation(summary = "상품 삭제", description = "상품을 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 상품이 아닌 경우)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{productId}")
    ResponseEntity<CommonResponse<Void>> deleteProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("productId") Long productId
    );
}
