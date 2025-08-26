package FreshBid.back.event.listener;

import FreshBid.back.entity.AuctionHistory;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import FreshBid.back.event.OrderStatusChangedEvent;
import FreshBid.back.repository.AuctionHistoryRepository;
import FreshBid.back.service.EmailService;
import FreshBid.back.service.OrderService;
import java.util.Optional;
import FreshBid.back.service.PriceDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final PriceDataService priceDataService;
    private final EmailService emailService;

    @Async
    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        log.info("주문 상태 변경 이벤트 처리 - 주문 ID: {}, 이전 상태: {}, 새 상태: {}",
            order.getId(), event.getPreviousStatus(), event.getNewStatus());

        try {
            // 이메일 발송 (구매자)
            emailService.sendOrderEmail(order.getCustomer(), order);

            // 이메일 발송 (판매자)
            emailService.sendOrderEmail(order.getSeller(), order);

            // 취소/환불 시 차순위 입찰자 처리
            if (event.getNewStatus() == OrderStatus.CANCELLED
                || event.getNewStatus() == OrderStatus.REFUNDED) {

                // 1. 현재 Order의 AuctionHistory를 '포기'로 변경
                AuctionHistory currentHistory = order.getAuctionHistory();
                currentHistory.setAction(AuctionHistory.Action.포기);
                auctionHistoryRepository.save(currentHistory);

                log.debug("AuctionHistory 상태 변경 완료 - Order ID: {}, Action: 포기", order.getId());

                // 2. 차순위 입찰자 처리 시작
                Optional<AuctionHistory> nextBidderOpt = orderService.processNextBidder(
                    order.getAuction().getId());

                // 3. 새 주문 생성 (차순위 입찰자가 있는 경우에만)
                if (nextBidderOpt.isPresent()) {
                    AuctionHistory nextBidder = nextBidderOpt.get();
                    orderService.createOrder(nextBidder);
                    log.info("차순위 입찰자 주문 생성 성공 - Auction ID: {}, User ID: {}",
                        order.getAuction().getId(), nextBidder.getUserId());
                } else {
                    log.info("차순위 입찰자 없음 - 유찰 처리 완료 - Auction ID: {}",
                        order.getAuction().getId());
                }
            }

        // 주문 완료 시 가격 데이터 추가
        if (event.getNewStatus() == OrderStatus.COMPLETED) {
            try {
                priceDataService.addPriceDataFromOrder(order);
                log.info("주문 완료로 가격 데이터 추가 완료 - 주문 ID: {}", order.getId());
            } catch (Exception e) {
                log.error("가격 데이터 추가 중 오류 발생 - 주문 ID: {}", order.getId(), e);
            }
        }

        } catch (Exception e) {
            log.error("주문 상태 변경 이벤트 처리 중 오류 발생 - 주문 ID: {}", event.getOrder().getId(), e);
        }
    }
}
