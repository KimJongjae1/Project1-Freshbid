package FreshBid.back.controller.impl;

import FreshBid.back.controller.LiveController;
import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.service.LiveService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Live 및 경매 API 구현체
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auction/live")
public class LiveControllerImpl implements LiveController {

    private final LiveService liveService;

    @Override
    @PostMapping("")
    public ResponseEntity<CommonResponse<LiveResponseDto>> registerLive(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @ModelAttribute LiveCreateRequestDto liveCreateRequestDto) throws JsonProcessingException {
        log.info("Live 등록 요청 - 요청 데이터: {}", liveCreateRequestDto);

        ObjectMapper objectMapper = new ObjectMapper();
        List<AuctionRequestDto> auctions = objectMapper.readValue(
                liveCreateRequestDto.getAuctionsJson(), new TypeReference<List<AuctionRequestDto>>() {}
        );
        liveCreateRequestDto.setAuctions(auctions);
        User currentUser = userDetails.getUser();

        LiveResponseDto liveResponseDto = liveService.createLiveWithAuctions(liveCreateRequestDto,
            currentUser);
        log.info("Live 등록 완료 - Live ID: {}, 사용자 ID: {}", liveResponseDto.getId(),
            currentUser.getId());

        CommonResponse<LiveResponseDto> response = CommonResponse.<LiveResponseDto>builder()
            .success(true).message("Live 등록에 성공했습니다.").data(liveResponseDto).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("")
    public ResponseEntity<CommonResponse<Page<LiveResponseDto>>> getAllLives(
        LiveSearchRequestDto searchRequest) {
        log.info("Live 검색 요청 - 요청 정보 : {}", searchRequest.toString());

        // 검색 수행
        Page<LiveResponseDto> searchResult = liveService.searchLives(searchRequest);
        log.info("Live 검색 완료 - 총 {}개 중 {}개 조회", searchResult.getTotalElements(),
            searchResult.getNumberOfElements());

        CommonResponse<Page<LiveResponseDto>> response = CommonResponse.<Page<LiveResponseDto>>builder()
            .success(true).message("Live 목록 조회에 성공했습니다.").data(searchResult).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{liveId}")
    public ResponseEntity<CommonResponse<LiveResponseDto>> getLive(
        @PathVariable("liveId") Long liveId) {
        log.info("Live 조회 요청 - Live ID: {}", liveId);

        LiveResponseDto liveResponseDto = liveService.getLiveWithDetails(liveId);
        log.debug("Live 조회 완료 - Live ID: {}", liveId);

        CommonResponse<LiveResponseDto> response = CommonResponse.<LiveResponseDto>builder()
            .success(true).message("Live 정보 조회에 성공했습니다.").data(liveResponseDto).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{liveId}")
    public ResponseEntity<CommonResponse<Void>> updateLive(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("liveId") Long liveId,
        @RequestBody LiveUpdateRequestDto liveUpdateRequestDto) {
        log.info("Live 수정 요청 - Live ID: {}, 요청 데이터: {}", liveId, liveUpdateRequestDto);

        Long currentUserId = userDetails.getUser().getId();

        liveService.updateLive(currentUserId, liveId, liveUpdateRequestDto);
        log.debug("Live 수정 완료 - Live ID: {}", liveId);

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
            .message("Live 수정에 성공했습니다.").data(null).build();
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{liveId}")
    public ResponseEntity<CommonResponse<Void>> deleteLive(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("liveId") Long liveId) {
        log.info("Live 삭제 요청 - Live ID: {}", liveId);

        Long currentUserId = userDetails.getUser().getId();

        liveService.deleteLive(currentUserId, liveId);

        log.debug("Live 삭제 완료 - Live ID: {}", liveId);

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
            .message("Live 삭제에 성공했습니다.").data(null).build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<Boolean>> checkLiveOwnership(
        @AuthenticationPrincipal FreshBidUserDetails userDetails, Long liveId) {
        log.info("Live 소유자 검사 요청 - Live ID: {}", liveId);

        Long currentUserId = userDetails.getUser().getId();

        boolean result = liveService.checkOwner(currentUserId, liveId);

        log.debug("Live 소유자 검사 완료 - Live ID: {}", liveId);

        CommonResponse<Boolean> response = CommonResponse.<Boolean>builder()
            .success(true)
            .message("Live 소유자 검사를 성공했습니다.")
            .data(result)
            .build();
        return ResponseEntity.ok(response);
    }
}
