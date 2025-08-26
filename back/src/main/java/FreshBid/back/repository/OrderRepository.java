package FreshBid.back.repository;

import FreshBid.back.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 구매자별 주문 목록 조회
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * 판매자별 주문 목록 조회
     */
    List<Order> findBySellerId(Long sellerId);

    /**
     * 경매별 주문 목록 조회
     */
    List<Order> findByAuctionId(Long auctionId);

    /**
     * AuctionHistory로 주문 조회
     */
    Optional<Order> findByAuctionHistoryId(Long auctionHistoryId);
}
