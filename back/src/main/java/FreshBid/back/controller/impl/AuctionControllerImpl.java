package FreshBid.back.controller.impl;

import FreshBid.back.controller.AuctionController;
import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.dto.bid.BidRequestDto;
import FreshBid.back.dto.bid.BidResponseDto;
import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.Auction;
import FreshBid.back.service.AuctionService;
import FreshBid.back.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auction")
public class AuctionControllerImpl implements AuctionController {

    private final BidService bidService;
    private final AuctionService auctionService;

    @Override
    @GetMapping("")
    public ResponseEntity<CommonResponse<Page<AuctionResponseDto>>> getAllAuctions(
        @Valid AuctionSearchRequestDto searchRequest) {
        log.info("경매 검색 요청 - 요청 정보 : {}", searchRequest.toString());

        // 검색 수행
        Page<AuctionResponseDto> searchResult = auctionService.searchAuctions(searchRequest);
        log.info("경매 검색 완료 - 총 {}개 중 {}개 조회", searchResult.getTotalElements(),
            searchResult.getNumberOfElements());

        CommonResponse<Page<AuctionResponseDto>> response = CommonResponse.<Page<AuctionResponseDto>>builder()
            .success(true).message("경매 목록 조회에 성공했습니다.").data(searchResult).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{auctionId}")
    public ResponseEntity<CommonResponse<AuctionResponseDto>> getAuction(
        @PathVariable("auctionId") Long auctionId) {
        log.info("경매 조회 요청 - 경매 ID: {}", auctionId);

        AuctionResponseDto auctionResponseDto = auctionService.getAuction(auctionId);
        log.debug("경매 조회 완료 - 경매 ID: {}", auctionId);

        CommonResponse<AuctionResponseDto> response = CommonResponse.<AuctionResponseDto>builder()
            .success(true).message("경매 조회에 성공했습니다.").data(auctionResponseDto).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("")
    public ResponseEntity<CommonResponse<AuctionResponseDto>> createAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @RequestBody Long liveId,
        @RequestBody @Valid AuctionRequestDto auctionRequestDto) {
        log.info("경매 등록 요청 - Live ID: {}", liveId);

        AuctionResponseDto auctionResponseDto = auctionService.createAuctionByLiveId(
            auctionRequestDto, liveId, userDetails.getUser().getId());

        CommonResponse<AuctionResponseDto> response = CommonResponse.<AuctionResponseDto>builder()
            .success(true)
            .message("경매 생성에 성공했습니다.")
            .data(auctionResponseDto)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PutMapping("/{auctionId}")
    public ResponseEntity<CommonResponse<Void>> updateAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId,
        @RequestBody AuctionUpdateRequestDto auctionUpdateRequestDto) {
        log.info("경매 수정 요청 - 경매 ID: {}", auctionId);

        Long currentUserId = userDetails.getUser().getId();

        auctionService.updateAuction(currentUserId, auctionId, auctionUpdateRequestDto);
        log.debug("경매 수정 완료 - 경매 ID: {}", auctionId);

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
            .message("경매 수정에 성공했습니다.").data(null).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<CommonResponse<Void>> deleteAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId) {
        log.info("경매 삭제 요청 - 경매 ID: {}", auctionId);

        Long currentUserId = userDetails.getUser().getId();

        auctionService.deleteAuction(currentUserId, auctionId);

        log.debug("경매 삭제 완료 - 경매 ID: {}", auctionId);

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
            .message("경매 삭제에 성공했습니다.").data(null).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("{auctionId}/start")
    public ResponseEntity<CommonResponse<Void>> startAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId) {
        log.info("경매 시작 요청 - 경매 ID: {}", auctionId);

        Long currentUserId = userDetails.getUser().getId();

        auctionService.validateAndChangeAuctionStatus(Auction.Status.SCHEDULED,
            Auction.Status.IN_PROGRESS, currentUserId, auctionId);
        log.debug("경매 시작 완료 - 경매 ID: {}", auctionId);

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
            .message("경매 시작에 성공했습니다.").data(null).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{auctionId}/end")
    public ResponseEntity<CommonResponse<BidResponseDto>> endAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId) {
        log.info("경매 종료 요청 - 경매 ID: {}", auctionId);

        Long currentUserId = userDetails.getUser().getId();

        auctionService.validateAndChangeAuctionStatus(Auction.Status.IN_PROGRESS,
            Auction.Status.ENDED, currentUserId, auctionId);
        log.debug("경매 종료 완료 - 경매 ID: {}", auctionId);

        BidResponseDto result = bidService.finalizeBid(auctionId);
        StringBuilder message = new StringBuilder("경매 종료에 성공했습니다.");
        if (result == null) {
            message.append(" 입찰 내역이 존재하지 않습니다.");
        }

        CommonResponse<BidResponseDto> response = CommonResponse.<BidResponseDto>builder()
            .success(true)
            .message(message.toString()).data(result).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<CommonResponse<Void>> createBid(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId,
        @Valid @RequestBody BidRequestDto bidRequestDto) {

        log.info("입찰 제출 요청 - 경매 ID: {}, 사용자: {}, 입찰가: {}",
            auctionId, userDetails.getUser().getId(), bidRequestDto.getBidPrice());

        bidService.createBid(auctionId, userDetails.getUser(), bidRequestDto);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(true).message("입찰이 성공적으로 제출되었습니다.").data(null).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{auctionId}/bid")
    public ResponseEntity<CommonResponse<BidStatusDto>> getAllBids(
        @PathVariable("auctionId") Long auctionId) {

        log.info("전체 입찰 현황 조회 요청 - 경매 ID: {}", auctionId);

        BidStatusDto result = bidService.getBidStatus(auctionId, 0);

        CommonResponse<BidStatusDto> response = CommonResponse.<BidStatusDto>builder()
            .success(true).message("입찰 현황을 성공적으로 조회하였습니다.").data(result).build();

        return ResponseEntity.ok(response);
    }
}
