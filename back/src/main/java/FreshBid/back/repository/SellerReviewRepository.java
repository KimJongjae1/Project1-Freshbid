package FreshBid.back.repository;

import FreshBid.back.entity.SellerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerReviewRepository extends JpaRepository<SellerReview, Long> {
    // ⚠️ 주의: 삭제된 리뷰도 포함하여 확인합니다. is_deleted 처리가 필요합니다.
    boolean existsByOrderId(Long orderId);
    
    // ✅ 권장: 삭제되지 않은 리뷰만 확인합니다.
    boolean existsByOrderIdAndIsDeletedFalse(Long orderId);
    
    // 특정 사용자가 특정 주문에 대해 삭제되지 않은 리뷰가 있는지 확인
    boolean existsByOrderIdAndUserIdAndIsDeletedFalse(Long orderId, Long userId);
    
    // 삭제되지 않은 리뷰만 조회
    Optional<SellerReview> findByIdAndIsDeletedFalse(Long id);
    
    // 부모 리뷰의 댓글들 조회 (삭제되지 않은 것만)
    List<SellerReview> findBySuperReviewAndIsDeletedFalse(SellerReview superReview);
}
