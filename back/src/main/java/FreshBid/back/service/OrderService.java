package FreshBid.back.service;

import FreshBid.back.dto.Order.OrderDetailResponseDto;
import FreshBid.back.dto.Order.OrderResponseDto;
import FreshBid.back.dto.Order.OrderUpdateRequestDto;
import FreshBid.back.entity.AuctionHistory;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.User;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    void createOrder(AuctionHistory auctionHistory);

    OrderResponseDto updateOrderStatus(User user, OrderUpdateRequestDto orderUpdateRequestDto);

    OrderDetailResponseDto getOrderById(User currentUser, Long orderId);

    List<OrderResponseDto> getOrdersByUser(User currentUser);

    List<OrderResponseDto> getOrdersByAuctionId(User currentUser, Long auctionId);

    Optional<AuctionHistory> processNextBidder(Long auctionId);
}
