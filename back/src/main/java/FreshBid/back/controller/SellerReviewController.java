package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.SellerReview.*;
import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매자 후기 API", description = "판매자 후기 등록, 조회, 수정, 삭제 관련 API")
@RequestMapping("/auction/review")
public interface SellerReviewController {

    @Operation(summary = "후기 등록", description = "판매자 후기를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "후기 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주문"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    ResponseEntity<CommonResponse<Void>> createReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @RequestBody SellerReviewCreateRequestDto requestDto
    );

    @Operation(summary = "판매자 후기 전체 조회", description = "판매자 ID에 해당하는 후기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/seller/{sellerId}")
    ResponseEntity<CommonResponse<Page<SellerReviewResponseDto>>> getReviewsBySeller(
            @PathVariable Long sellerId,
            SellerReviewSearchRequestDto searchDto
    );

    @Operation(summary = "후기 수정", description = "본인이 작성한 후기를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 후기 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 후기"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{reviewId}")
    ResponseEntity<CommonResponse<Void>> updateReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody SellerReviewUpdateRequestDto requestDto
    );

    @Operation(summary = "후기 삭제", description = "본인이 작성한 후기를 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 후기 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 후기"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{reviewId}")
    ResponseEntity<CommonResponse<Void>> deleteReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Long reviewId
    );
}
