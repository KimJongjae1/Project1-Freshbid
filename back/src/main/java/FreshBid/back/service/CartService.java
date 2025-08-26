package FreshBid.back.service;

import java.util.List;

import FreshBid.back.dto.cart.CartProductDto;

public interface CartService {
    public List<CartProductDto> getCartlist(Long userId);
}
