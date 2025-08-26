package FreshBid.back.controller;

import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.dto.cart.CartProductDto;
import FreshBid.back.dto.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Cart", description = "장바구니(찜 목록) 관련 API")
@RequestMapping("/cart")
public interface CartController {

        @Operation(summary = "장바구니(찜 목록) 조회", description = "인증된 사용자의 찜 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "장바구니 조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                        @ApiResponse(responseCode = "403", description = "유효하지 않은 jwt token", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
        })
        @GetMapping
        ResponseEntity<CommonResponse<List<CartProductDto>>> getWishlist(
                        @AuthenticationPrincipal FreshBidUserDetails userDetails);
}
