package FreshBid.back.controller.impl;

import FreshBid.back.controller.SellerQnaController;
import FreshBid.back.dto.SellerQna.*;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.service.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerQnaControllerImpl implements SellerQnaController {

    private final SellerQnaService sellerQnaService;

    @Override
    public ResponseEntity<CommonResponse<Void>> createQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            SellerQnaCreateRequestDto requestDto
    ) {
        User user = userDetails.getUser();
        sellerQnaService.createQna(requestDto, user);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 문의 등록에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Page<SellerQnaResponseDto>>> getQnasBySeller(
            Long sellerId,
            SellerQnaSearchRequestDto searchDto
    ) {
        searchDto.setSellerId(sellerId);
        Page<SellerQnaResponseDto> qnas = sellerQnaService.getQnasBySeller(searchDto);

        CommonResponse<Page<SellerQnaResponseDto>> response = CommonResponse.<Page<SellerQnaResponseDto>>builder()
                .success(true)
                .message("판매자 문의 목록 조회에 성공했습니다.")
                .data(qnas)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<SellerQnaResponseDto>> getQnaById(Long qnaId) {
        SellerQnaResponseDto qna = sellerQnaService.getQnaById(qnaId);

        CommonResponse<SellerQnaResponseDto> response = CommonResponse.<SellerQnaResponseDto>builder()
                .success(true)
                .message("판매자 문의 조회에 성공했습니다.")
                .data(qna)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> updateQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Long qnaId,
            SellerQnaUpdateRequestDto requestDto
    ) {
        Long userId = userDetails.getUser().getId();
        sellerQnaService.updateQna(qnaId, requestDto, userId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 문의 수정에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> deleteQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            Long qnaId
    ) {
        Long userId = userDetails.getUser().getId();
        sellerQnaService.deleteQna(qnaId, userId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true)
                .message("판매자 문의 삭제에 성공했습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
} 