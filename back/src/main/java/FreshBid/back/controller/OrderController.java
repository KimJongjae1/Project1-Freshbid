package FreshBid.back.controller;

import FreshBid.back.dto.Order.OrderUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "주문 관리 API", description = "주문 상태 변경, 조회를 위한 API입니다.")
@RequestMapping("/orders")
public interface OrderController {

    @Operation(summary = "주문 상태 변경", 
               description = "주문 상태를 변경합니다. " +
                           "구매자: WAITING→CANCELLED(취소), PAID/SHIPPED/COMPLETED→PENDING(환불요청) " +
                           "판매자: WAITING→CANCELLED, PAID→SHIPPED/REFUNDED, SHIPPED→COMPLETED/REFUNDED, COMPLETED→REFUNDED, PENDING→PAID/REFUNDED")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 전환 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{orderId}")
    ResponseEntity<CommonResponse<?>> updateOrderStatus(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable("orderId") Long orderId,
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @Valid @RequestBody OrderUpdateRequestDto requestDto
    );

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다. (구매자, 판매자, 관리자만 접근 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{orderId}")
    ResponseEntity<CommonResponse<?>> getOrderById(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable("orderId") Long orderId,
        @AuthenticationPrincipal FreshBidUserDetails userDetails
    );

    @Operation(summary = "주문 목록 조회", description = "현재 사용자의 주문 목록을 조회합니다. (구매자: 구매한 주문, 판매자: 판매한 주문)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("")
    ResponseEntity<CommonResponse<?>> getOrdersByUser(
        @AuthenticationPrincipal FreshBidUserDetails userDetails
    );

    @Operation(summary = "경매별 주문 조회", description = "특정 경매의 모든 주문을 조회합니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "경매별 주문 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/auction/{auctionId}")
    ResponseEntity<?> getOrdersByAuction(
        @Parameter(description = "경매 ID", example = "100")
        @PathVariable("auctionId") Long auctionId,
        @AuthenticationPrincipal FreshBidUserDetails userDetails
    );
}