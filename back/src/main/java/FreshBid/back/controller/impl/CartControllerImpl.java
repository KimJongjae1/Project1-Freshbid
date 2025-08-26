package FreshBid.back.controller.impl;

import FreshBid.back.controller.CartController;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.dto.cart.CartProductDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.service.CartService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartControllerImpl implements CartController {

    private final CartService cartService;

    @Override
    public ResponseEntity<CommonResponse<List<CartProductDto>>> getWishlist(
            @AuthenticationPrincipal FreshBidUserDetails userDetails) {

        Long currentUserId = userDetails.getUser().getId();

        List<CartProductDto> cartResponse = cartService.getCartlist(currentUserId);

        CommonResponse<List<CartProductDto>> response = CommonResponse.<List<CartProductDto>>builder()
                .success(true)
                .message("장바구니 조회에 성공했습니다.")
                .data(cartResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
