package FreshBid.back.controller.impl;

import FreshBid.back.controller.ProductController;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.product.*;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auction/product")
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;

    @Override
    @PostMapping("")
    public ResponseEntity<CommonResponse<ProductResponseDto>> createProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @ModelAttribute ProductCreateRequestDto createDto
    ) {
        User currentUser = userDetails.getUser();
        ProductResponseDto result = productService.createProduct(createDto, currentUser);
        CommonResponse<ProductResponseDto> response = CommonResponse.<ProductResponseDto>builder()
                .success(true)
                .message("상품 등록에 성공했습니다.")
                .data(result)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("")
    public ResponseEntity<CommonResponse<Page<ProductResponseDto>>> searchProducts(
            ProductSearchRequestDto searchRequest
    ) {
        Page<ProductResponseDto> page = productService.searchProducts(searchRequest);
        CommonResponse<Page<ProductResponseDto>> response = CommonResponse.<Page<ProductResponseDto>>builder()
                .success(true)
                .message("상품 목록 조회에 성공했습니다.")
                .data(page)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponseDto>> getProduct(
            @PathVariable("productId") Long productId
    ) {
        ProductResponseDto dto = productService.getProduct(productId);
        CommonResponse<ProductResponseDto> response = CommonResponse.<ProductResponseDto>builder()
                .success(true)
                .message("상품 조회에 성공했습니다.")
                .data(dto)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{productId}")
    public ResponseEntity<CommonResponse<Void>> updateProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @RequestBody ProductUpdateRequestDto updateDto
    ) {
        Long sellerId = userDetails.getUser().getId();
        productService.updateProduct(sellerId, productId, updateDto);
        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("상품 수정에 성공했습니다.")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{productId}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("productId") Long productId
    ) {
        Long sellerId = userDetails.getUser().getId();
        productService.deleteProduct(sellerId, productId);
        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("상품 삭제에 성공했습니다.")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
