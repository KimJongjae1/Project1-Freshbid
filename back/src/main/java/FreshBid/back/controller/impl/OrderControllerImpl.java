package FreshBid.back.controller.impl;

import FreshBid.back.controller.OrderController;
import FreshBid.back.dto.Order.OrderDetailResponseDto;
import FreshBid.back.dto.Order.OrderResponseDto;
import FreshBid.back.dto.Order.OrderUpdateRequestDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;

    private final FileStorageService fileStorageService;
    @Override
    @PutMapping("/{orderId}")
    public ResponseEntity<CommonResponse<?>> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @Valid @RequestBody OrderUpdateRequestDto requestDto) {
        
        log.info("주문 상태 변경 요청 - Order ID: {}, User ID: {}, New Status: {}", 
                orderId, userDetails.getUser().getId(), requestDto.getOrderStatus());

        User user = userDetails.getUser();
        requestDto.setOrderId(orderId);

        OrderResponseDto result = orderService.updateOrderStatus(user, requestDto);

        CommonResponse<OrderResponseDto> response = CommonResponse.<OrderResponseDto>builder()
                .success(true)
                .message("주문 상태가 성공적으로 변경되었습니다.")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{orderId}")
    public ResponseEntity<CommonResponse<?>> getOrderById(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal FreshBidUserDetails userDetails) {
        
        log.info("주문 상세 조회 요청 - Order ID: {}, User ID: {}", 
                orderId, userDetails.getUser().getId());

        User user = userDetails.getUser();
        OrderDetailResponseDto result = orderService.getOrderById(user, orderId);

        //주문 기록 이미지에 상품 사진
        String url = fileStorageService.getUrl(result.getProduct().getReprImgSrc());
        result.getProduct().setReprImgSrc(fileStorageService.convertImageUrlToBlob(url));

        CommonResponse<OrderDetailResponseDto> response = CommonResponse.<OrderDetailResponseDto>builder()
                .success(true)
                .message("주문 조회가 성공했습니다.")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("")
    public ResponseEntity<CommonResponse<?>> getOrdersByUser(
            @AuthenticationPrincipal FreshBidUserDetails userDetails) {
        
        log.info("사용자 주문 목록 조회 요청 - User ID: {}, Role: {}", 
                userDetails.getUser().getId(), userDetails.getUser().getRole());

        User user = userDetails.getUser();
        List<OrderResponseDto> result = orderService.getOrdersByUser(user);

        CommonResponse<List<OrderResponseDto>> response = CommonResponse.<List<OrderResponseDto>>builder()
                .success(true)
                .message("주문 목록 조회가 성공했습니다.")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<?> getOrdersByAuction(
            @PathVariable("auctionId") Long auctionId,
            @AuthenticationPrincipal FreshBidUserDetails userDetails) {
        
        log.info("경매별 주문 조회 요청 - Auction ID: {}, User ID: {}", 
                auctionId, userDetails.getUser().getId());

        User user = userDetails.getUser();
        List<OrderResponseDto> result = orderService.getOrdersByAuctionId(user, auctionId);

        CommonResponse<List<OrderResponseDto>> response = CommonResponse.<List<OrderResponseDto>>builder()
                .success(true)
                .message("경매별 주문 조회가 성공했습니다.")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}