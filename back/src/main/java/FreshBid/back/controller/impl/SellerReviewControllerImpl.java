package FreshBid.back.controller.impl;

import FreshBid.back.controller.SellerReviewController;
import FreshBid.back.dto.SellerReview.*;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.service.SellerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerReviewControllerImpl implements SellerReviewController {

    private final SellerReviewService sellerReviewService;

    @Override
    public ResponseEntity<CommonResponse<Void>> createReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            SellerReviewCreateRequestDto requestDto
    ) {
        User user = userDetails.getUser();
        sellerReviewService.createReview(requestDto, user);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 후기 등록에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Page<SellerReviewResponseDto>>> getReviewsBySeller(
            Long sellerId,
            SellerReviewSearchRequestDto searchDto
    ) {
        searchDto.setSellerId(sellerId);
        Page<SellerReviewResponseDto> reviews = sellerReviewService.getReviewsBySeller(searchDto);

        CommonResponse<Page<SellerReviewResponseDto>> response = CommonResponse.<Page<SellerReviewResponseDto>>builder()
                .success(true)
                .message("판매자 후기 목록 조회에 성공했습니다.")
                .data(reviews)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> updateReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Long reviewId,
            SellerReviewUpdateRequestDto requestDto
    ) {
        Long userId = userDetails.getUser().getId();
        sellerReviewService.updateReview(reviewId, requestDto, userId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 후기 수정에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> deleteReview(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Long reviewId
    ) {
        Long userId = userDetails.getUser().getId();
        sellerReviewService.deleteReview(reviewId, userId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 후기 삭제에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
