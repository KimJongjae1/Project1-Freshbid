package FreshBid.back.repository;

import FreshBid.back.entity.SellerQna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {
    
    // 삭제되지 않은 문의만 조회
    Optional<SellerQna> findByIdAndIsDeletedFalse(Long id);
} 