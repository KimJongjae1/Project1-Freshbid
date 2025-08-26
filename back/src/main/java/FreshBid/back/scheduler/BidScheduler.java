package FreshBid.back.scheduler;

import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.BidRedisRepositorySupport;
import FreshBid.back.service.BidService;
import FreshBid.back.socket.SignalingHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidScheduler {

    private final BidRedisRepositorySupport bidRedisRepositorySupport;
    private final BidService bidService;
    private final SignalingHandler signalingHandler;

    /**
     * 3초마다 진행 중인 경매들의 입찰 상태를 조회하여 WebSocket으로 브로드캐스트
     */
    @Scheduled(fixedRate = 3000)
    public void broadcastBidUpdates() {
        try {
            // 1. Redis에서 진행 중인 경매들 조회 (auctionId -> roomId 매핑)
            Map<Long, Long> activeAuctions = bidRedisRepositorySupport.getAllActiveAuctions();

            if (activeAuctions.isEmpty()) {
                log.debug("진행 중인 경매가 없습니다.");
                return;
            }

            log.debug("진행 중인 경매 수: {}", activeAuctions.size());

            // 2. 각 경매의 입찰 상태 조회 및 브로드캐스트
            for (Map.Entry<Long, Long> entry : activeAuctions.entrySet()) {
                Long auctionId = entry.getKey();
                Long roomId = entry.getValue();

                try {
                    // 2-1. 현재 경매의 입찰 상태 조회 (TOP 10)
                    BidStatusDto bidStatus = bidService.getBidStatus(auctionId, 10);

                    // 2-2. 다음 최소 입찰가(현재 최고가+1)를 다음 3초 구간의 최소 입찰가로 Redis에 저장
                    Long currentHighestPrice = bidStatus.getCurrentHighestPrice();
                    bidRedisRepositorySupport.setCurrentMinBidPrice(auctionId,
                        currentHighestPrice + 1);

                    // 2-3. 해당 룸의 모든 참가자에게 브로드캐스트
                    signalingHandler.broadcastBidStatusToRoom(roomId, bidStatus);

                    log.debug("경매 상태 브로드캐스트 완료 - 경매 ID: {}, 룸 ID: {}, 현재 최고가: {}",
                        auctionId, roomId, currentHighestPrice);

                } catch (NotFoundException e) {
                    // 경매가 존재하지 않는 경우 Redis에서 관련 데이터 정리
                    log.warn("존재하지 않는 경매 {} (룸 {}) - Redis 데이터 정리 중", auctionId, roomId);
                    bidRedisRepositorySupport.removeActiveAuction(auctionId);
                    bidRedisRepositorySupport.removeCurrentMinBidPrice(auctionId);
                    bidRedisRepositorySupport.deleteByAuctionId(auctionId);
                    log.info("경매 {} Redis 데이터 정리 완료", auctionId);
                } catch (Exception e) {
                    log.error("경매 {} (룸 {}) 상태 브로드캐스트 실패", auctionId, roomId, e);
                }
            }

        } catch (Exception e) {
            log.error("입찰 상태 브로드캐스트 스케줄러 실행 중 오류 발생", e);
        }
    }
}