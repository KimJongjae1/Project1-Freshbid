package FreshBid.back.event;

import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderStatusChangedEvent {
    
    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
}
