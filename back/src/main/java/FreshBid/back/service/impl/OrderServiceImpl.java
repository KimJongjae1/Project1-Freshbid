package FreshBid.back.service.impl;

import FreshBid.back.dto.Order.OrderDetailResponseDto;
import FreshBid.back.dto.Order.OrderResponseDto;
import FreshBid.back.dto.Order.OrderUpdateRequestDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.AuctionHistory;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import FreshBid.back.entity.User;
import FreshBid.back.entity.User.Role;
import FreshBid.back.event.OrderStatusChangedEvent;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.AuctionHistoryRepository;
import FreshBid.back.repository.AuctionRepository;
import FreshBid.back.repository.AuctionRepositorySupport;
import FreshBid.back.repository.OrderRepository;
import FreshBid.back.repository.OrderRepositorySupport;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.service.EmailService;
import FreshBid.back.service.OrderService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderRepositorySupport orderRepositorySupport;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final AuctionRepositorySupport auctionRepositorySupport;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createOrder(AuctionHistory auctionHistory) {
        log.info("주문 생성 시작 - AuctionHistory ID: {}, Auction ID: {}, User ID: {}",
            auctionHistory.getId(), auctionHistory.getAuctionId(), auctionHistory.getUserId());

        // 1. 연관 엔티티 조회
        Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(
            auctionHistory.getAuctionId());
        if (auction == null) {
            log.warn("경매를 찾을 수 없음 - Auction ID: {}", auctionHistory.getAuctionId());
            throw new NotFoundException("경매를 찾을 수 없습니다: " + auctionHistory.getAuctionId());
        }

        User customer = userRepository.findById(auctionHistory.getUserId())
            .orElseThrow(() -> {
                log.warn("사용자를 찾을 수 없음 - User ID: {}", auctionHistory.getUserId());
                return new NotFoundException("사용자를 찾을 수 없습니다: " + auctionHistory.getUserId());
            });

        // Product는 Auction을 통해 접근
        User seller = auction.getProduct().getUser();

        log.debug("연관 엔티티 조회 완료 - Customer: {}, Seller: {}, Product: {}",
            customer.getId(), seller.getId(), auction.getProduct().getId());

        // 2. 기존 주문 존재 여부 확인 - 중복 주문 방지
        Optional<Order> existingOrder = orderRepository.findByAuctionHistoryId(
            auctionHistory.getId());
        if (existingOrder.isPresent()) {
            log.warn("이미 존재하는 주문 - AuctionHistory ID: {}, Order ID: {}",
                auctionHistory.getId(), existingOrder.get().getId());
            throw new IllegalStateException("해당 낙찰에 대한 주문이 이미 존재합니다");
        }

        // 3. Order 엔티티 생성
        Order order = new Order();
        order.setAuctionHistory(auctionHistory);
        order.setAuction(auction);
        order.setSeller(seller);
        order.setCustomer(customer);
        order.setPrice(auctionHistory.getPrice());
        order.setStatus(OrderStatus.WAITING);

        // 4. Order 저장
        order = orderRepository.save(order);
        log.debug("Order 저장 완료 - Order ID: {}", order.getId());

        // 5. AuctionHistory Action을 '결제완료'로 변경
        auctionHistory.setAction(AuctionHistory.Action.결제완료);
        auctionHistoryRepository.save(auctionHistory);
        log.debug("AuctionHistory Action 변경 완료 - Action: 결제완료");

        publishOrderStatusChangedEvent(order, null);

        log.info("주문 생성 완료 - Order ID: {}, Customer: {}, Seller: {}",
            order.getId(), customer.getId(), seller.getId());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(User user,
        OrderUpdateRequestDto orderUpdateRequestDto) {
        log.info("주문 상태 변경 시작 - User ID: {}, Order ID: {}, New Status: {}",
            user.getId(), orderUpdateRequestDto.getOrderId(),
            orderUpdateRequestDto.getOrderStatus());

        try {
            // 1. Order 조회
            Order order = orderRepository.findById(orderUpdateRequestDto.getOrderId())
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없음 - Order ID: {}", orderUpdateRequestDto.getOrderId());
                    return new NotFoundException(
                        "주문을 찾을 수 없습니다: " + orderUpdateRequestDto.getOrderId());
                });

            // 2. 주문 접근 권한 검증
            validateOrderAccess(user, order);
            OrderStatus previousStatus = order.getStatus();
            OrderStatus newStatus = orderUpdateRequestDto.getOrderStatus();

            log.debug("주문 상태 변경 검증 - Previous: {}, New: {}, User Role: {}",
                previousStatus, newStatus, user.getRole());

            // 3. 상태 전환 검증
            validateStatusTransition(previousStatus, newStatus, user.getRole());

            // 4. Order 상태 업데이트
            order.setStatus(newStatus);
            order = orderRepository.save(order);

            log.debug("주문 상태 변경 완료 - Order ID: {}, Status: {} → {}",
                order.getId(), previousStatus, newStatus);

            // 5. 이벤트 발행
            publishOrderStatusChangedEvent(order, previousStatus);

            log.info("주문 상태 변경 성공 - Order ID: {}, Status: {} → {}",
                order.getId(), previousStatus, newStatus);

            return OrderResponseDto.from(order);

        } catch (Exception e) {
            log.error("주문 상태 변경 중 오류 발생 - Order ID: {}", orderUpdateRequestDto.getOrderId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderById(User currentUser, Long orderId) {
        log.debug("주문 상세 조회 시작 - Order ID: {}", orderId);

        Order order = orderRepositorySupport.findByIdWithDetail(orderId);
        if (order == null) {
            log.warn("주문을 찾을 수 없음 - Order ID: {}", orderId);
            throw new NotFoundException("주문을 찾을 수 없습니다: " + orderId);
        }

        validateOrderAccess(currentUser, order);

        List<OrderStatus> allowedTransitions = getAllowedTransitions(
            order.getStatus(), order.getCustomer().getRole());

        log.debug("주문 상세 조회 완료 - Order ID: {}, Status: {}", orderId, order.getStatus());

        return OrderDetailResponseDto.from(order, allowedTransitions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUser(User currentUser) {
        log.debug("사용자 주문 목록 조회 시작 - User ID: {}, Role: {}", currentUser.getId(),
            currentUser.getRole());

        return switch (currentUser.getRole()) {
            case ROLE_CUSTOMER -> findOrdersByCustomerId(currentUser.getId());
            case ROLE_SELLER -> findOrdersBySellerId(currentUser.getId());
            case ROLE_ADMIN -> findAllOrders();
            default -> {
                log.warn("지원하지 않는 사용자 역할 - User ID: {}, Role: {}", currentUser.getId(),
                    currentUser.getRole());
                throw new ForbiddenException(
                    "주문 목록 조회 권한이 없습니다.");
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByAuctionId(User currentUser, Long auctionId) {
        log.debug("경매별 주문 목록 조회 - Auction ID: {}", auctionId);
        if (currentUser.getRole() == Role.ROLE_CUSTOMER) {
            throw new ForbiddenException("관리자 혹은 판매자만 조회할 수 있습니다.");
        }

        List<Order> orders = orderRepositorySupport.findByAuctionIdWithAuctionAndHistoryAndSeller(
            auctionId);

        validateOrderAccess(currentUser, orders.get(0));

        log.debug("경매별 주문 목록 조회 완료 - Auction ID: {}, Count: {}", auctionId, orders.size());

        return orders.stream()
            .map(OrderResponseDto::from)
            .toList();
    }

    @Override
    @Transactional
    public Optional<AuctionHistory> processNextBidder(Long auctionId) {
        log.info("차순위 입찰자 처리 시작 - Auction ID: {}", auctionId);

        // 1. 차순위 입찰자 조회 (가격 내림차순, 시간 오름차순)
        Optional<AuctionHistory> nextBidderOpt = auctionHistoryRepository
            .findFirstByAuctionIdAndActionOrderByPriceDescActionTimeAsc(
                auctionId, AuctionHistory.Action.입찰);

        if (nextBidderOpt.isEmpty()) {
            log.info("차순위 입찰자 없음 - 유찰 처리 - Auction ID: {}", auctionId);

            // 판매자에게 유찰 이메일 발송
            Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(auctionId);
            if (auction == null){
                throw new NotFoundException("경매를 찾을 수 없습니다: " + auctionId);
            }

            User seller = auction.getProduct().getUser();

            emailService.sendAuctionFailureEmail(seller, auction);

            log.debug("유찰 이메일 발송 완료 - Seller ID: {}", seller.getId());

            return Optional.empty(); // 차순위 입찰자가 없으므로 empty 반환
        }

        // 2. 차순위 입찰자 선택
        AuctionHistory nextBidder = nextBidderOpt.get();
        log.debug("차순위 입찰자 발견 - User ID: {}, Price: {}",
            nextBidder.getUserId(), nextBidder.getPrice());

        // 3. 차순위 입찰자를 '낙찰'로 변경
        nextBidder.setAction(AuctionHistory.Action.낙찰);
        auctionHistoryRepository.save(nextBidder);

        log.debug("차순위 입찰자 낙찰 처리 완료 - User ID: {}", nextBidder.getUserId());
        return Optional.of(nextBidder);
    }


    private void validateOrderAccess(User user, Order order) {
        log.debug("주문 접근 권한 검증 시작 - User ID: {}, Order ID: {}", user.getId(), order.getId());

        boolean hasAccess =
            user.getRole() == Role.ROLE_ADMIN || order.getCustomer().getId().equals(user.getId()) ||
                order.getSeller().getId().equals(user.getId());

        if (!hasAccess) {
            log.warn("주문 접근 권한 없음 - User ID: {}, Order ID: {}", user.getId(), order.getId());
            throw new ForbiddenException("해당 주문에 접근할 권한이 없습니다");
        }

        log.debug("주문 접근 권한 검증 완료 - User ID: {}, Order ID: {}", user.getId(), order.getId());
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target, Role userRole) {
        log.debug("상태 전환 검증 시작 - Current: {}, Target: {}, Role: {}", current, target, userRole);

        List<OrderStatus> allowedTransitions = getAllowedTransitions(current, userRole);

        if (!allowedTransitions.contains(target)) {
            log.warn("허용되지 않는 상태 전환 - Current: {}, Target: {}, Role: {}, Allowed: {}",
                current, target, userRole, allowedTransitions);
            throw new IllegalArgumentException(
                String.format("현재 %s에서 %s로 전환할 수 없습니다. 가능한 상태: %s",
                    current, target, allowedTransitions));
        }

        log.debug("상태 전환 검증 완료 - Current: {}, Target: {}, Role: {}", current, target, userRole);
    }

    private List<OrderStatus> getAllowedTransitions(OrderStatus currentStatus, Role userRole) {
        return switch (userRole) {
            case ROLE_CUSTOMER -> switch (currentStatus) {
                case WAITING -> Arrays.asList(OrderStatus.CANCELLED);
                case PAID, SHIPPED, COMPLETED -> Arrays.asList(OrderStatus.PENDING);
                default -> Arrays.asList();
            };
            case ROLE_SELLER -> switch (currentStatus) {
                case WAITING -> Arrays.asList(OrderStatus.CANCELLED);
                case PAID -> Arrays.asList(OrderStatus.SHIPPED, OrderStatus.REFUNDED);
                case SHIPPED -> Arrays.asList(OrderStatus.COMPLETED, OrderStatus.REFUNDED);
                case COMPLETED -> Arrays.asList(OrderStatus.REFUNDED);
                case PENDING -> Arrays.asList(OrderStatus.PAID, OrderStatus.REFUNDED);
                default -> Arrays.asList();
            };
            case ROLE_ADMIN -> Arrays.asList(OrderStatus.values()); // ADMIN은 모든 상태 전환 가능
            default -> Arrays.asList();
        };
    }

    private void publishOrderStatusChangedEvent(Order order, OrderStatus oldStatus) {
        log.debug("주문 상태 변경 이벤트 발행 - Order ID: {}, Old: {}, New: {}",
            order.getId(), oldStatus, order.getStatus());

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
            order, oldStatus, order.getStatus());
        eventPublisher.publishEvent(event);

        log.debug("주문 상태 변경 이벤트 발행 완료 - Order ID: {}", order.getId());
    }


    // SonarLint 경고 해결을 위한 private 헬퍼 메서드들 (중첩 Transaction 혹은 Self-Injection 우회)
    private List<OrderResponseDto> findOrdersByCustomerId(Long customerId) {
        log.debug("구매자 주문 목록 조회 - Customer ID: {}", customerId);

        List<Order> orders = orderRepositorySupport.findByCustomerIdWithAuctionAndHistoryAndSeller(customerId);

        log.debug("구매자 주문 목록 조회 완료 - Customer ID: {}, Count: {}", customerId, orders.size());

        return orders.stream()
            .map(OrderResponseDto::from)
            .toList();
    }

    private List<OrderResponseDto> findOrdersBySellerId(Long sellerId) {
        log.debug("판매자 주문 목록 조회 - Seller ID: {}", sellerId);

        List<Order> orders = orderRepositorySupport.findBySellerIdWithAuctionAndHistoryAndSeller(sellerId);

        log.debug("판매자 주문 목록 조회 완료 - Seller ID: {}, Count: {}", sellerId, orders.size());

        return orders.stream()
            .map(OrderResponseDto::from)
            .toList();
    }

    private List<OrderResponseDto> findAllOrders() {
        log.debug("전체 주문 목록 조회");

        List<Order> orders = orderRepositorySupport.findAllWithAuctionAndHistoryAndSeller();

        log.debug("전체 주문 목록 조회 완료");

        return orders.stream()
            .map(OrderResponseDto::from)
            .toList();
    }
}
