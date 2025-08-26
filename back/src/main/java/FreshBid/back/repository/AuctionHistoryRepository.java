package FreshBid.back.repository;

import FreshBid.back.entity.AuctionHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {

    List<AuctionHistory> findByAuctionIdOrderByActionTimeDesc(Long auctionId);

    List<AuctionHistory> findByAuctionIdAndActionOrderByPriceDesc(Long auctionId,
        AuctionHistory.Action action);

    AuctionHistory findTopByAuctionIdAndActionOrderByPriceDesc(Long auctionId,
        AuctionHistory.Action action);

    boolean existsByAuctionIdAndAction(Long auctionId, AuctionHistory.Action action);

    /**
     * 차순위 입찰자 조회용 메서드 가격 내림차순, 입찰시간 오름차순으로 정렬하여 첫 번째 결과 반환
     */
    Optional<AuctionHistory> findFirstByAuctionIdAndActionOrderByPriceDescActionTimeAsc(
        Long auctionId, AuctionHistory.Action action);
}
