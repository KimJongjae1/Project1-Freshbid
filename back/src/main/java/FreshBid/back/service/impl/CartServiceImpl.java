package FreshBid.back.service.impl;

import FreshBid.back.dto.cart.CartProductDto;
import FreshBid.back.repository.OrderRepositorySupport;
import FreshBid.back.service.CartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderRepositorySupport orderRepositorySupport;

    @Override
    @Transactional(readOnly = true)
    public List<CartProductDto> getCartlist(Long userId) {
		
        return orderRepositorySupport
            .findAllOrderByUserId(userId);
    }
}
