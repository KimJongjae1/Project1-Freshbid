package FreshBid.back.service;

import FreshBid.back.dto.product.*;
import FreshBid.back.entity.User;
import org.springframework.data.domain.Page;

public interface ProductService {

    /**
     * 신규 상품 등록
     */
    ProductResponseDto createProduct(ProductCreateRequestDto dto, User seller);

    /**
     * 단건 조회
     */
    ProductResponseDto getProduct(Long productId);

    /**
     * 목록 조회 (검색 + 페이징)
     */
    Page<ProductResponseDto> searchProducts(ProductSearchRequestDto searchRequest);

    /**
     * 상품 수정
     */
    void updateProduct(Long sellerId, Long productId, ProductUpdateRequestDto dto);

    /**
     * 상품 삭제 (soft delete)
     */
    void deleteProduct(Long sellerId, Long productId);
}
