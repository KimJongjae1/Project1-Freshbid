package FreshBid.back.service;

import FreshBid.back.dto.SellerReview.*;
import FreshBid.back.entity.User;
import org.springframework.data.domain.Page;

public interface SellerReviewService {

    /**
     * 후기 등록
     */
    void createReview(SellerReviewCreateRequestDto dto, User user);

    /**
     * 후기 수정
     */
    void updateReview(Long reviewId, SellerReviewUpdateRequestDto dto, Long userId);

    /**
     * 후기 삭제
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 판매자 후기 목록 조회 (정렬/필터 포함)
     */
    Page<SellerReviewResponseDto> getReviewsBySeller(SellerReviewSearchRequestDto dto);

    /**
     * 후기 단건 조회
     */
    SellerReviewResponseDto getReviewById(Long reviewId);

    /**
     * 주문 기준 후기 존재 여부 확인
     */
    boolean existsByOrderId(Long orderId);
}
