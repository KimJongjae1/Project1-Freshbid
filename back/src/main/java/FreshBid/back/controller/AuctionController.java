package FreshBid.back.controller;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.dto.bid.BidRequestDto;
import FreshBid.back.dto.bid.BidResponseDto;
import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "경매 관리 API", description = "경매 관리, 입찰 관련 API")
@RequestMapping("/auction")
public interface AuctionController {

    @Operation(summary = "경매 검색 및 페이징 조회", description = "다양한 조건으로 경매를 검색하고 페이징된 결과를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("")
    ResponseEntity<CommonResponse<Page<AuctionResponseDto>>> getAllAuctions(
        AuctionSearchRequestDto searchRequest);

    @Operation(summary = "경매 정보 조회", description = "개별 경매 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{auctionId}")
    ResponseEntity<CommonResponse<AuctionResponseDto>> getAuction(
        @PathVariable("auctionId") Long auctionId);

    @Operation(summary = "경매 단독 등록", description = "개별 경매를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "경매 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검증 실패 등)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 Live가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 Live"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    ResponseEntity<CommonResponse<AuctionResponseDto>> createAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @RequestBody Long liveId,
        @RequestBody @Valid AuctionRequestDto auctionRequestDto);

    @Operation(summary = "경매 정보 수정", description = "이미 생성된 경매 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검증 실패 등)"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 경매가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{auctionId}")
    ResponseEntity<CommonResponse<Void>> updateAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId,
        @RequestBody AuctionUpdateRequestDto auctionUpdateRequestDto);

    @Operation(summary = "경매 삭제", description = "경매를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 경매가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{auctionId}")
    ResponseEntity<CommonResponse<Void>> deleteAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId);

    @Operation(summary = "경매 시작", description = "경매를 시작합니다. 경매 상태를 IN_PROGRESS로 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 시작 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 경매가 아님 / 경매 시작 전이 아님 등)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{auctionId}/start")
    ResponseEntity<CommonResponse<Void>> startAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId);

    @Operation(summary = "경매 시작", description = "경매를 종료합니다. 경매 상태를 ENDED로 변경합니다. 낙찰 정보를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "경매 시작 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 경매가 아님 / 경매 진행중이 아님 등)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{auctionId}/end")
    ResponseEntity<CommonResponse<BidResponseDto>> endAuction(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId);

    @Operation(summary = "입찰 제출", description = "해당 경매에 입찰가를 제출합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "입찰 제출 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입찰 데이터 (입찰 가격이 현재 최고가보다 낮음 등)"),
        @ApiResponse(responseCode = "403", description = "입찰 권한 없음 (구매자가 아님 / 경매 진행중이 아님 등)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{auctionId}/bid")
    ResponseEntity<CommonResponse<Void>> createBid(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("auctionId") Long auctionId,
        @Valid @RequestBody BidRequestDto bidRequestDto);

    @Operation(summary = "현재 입찰 현황 조회", description = "해당 경매의 실시간 입찰 목록 및 현재 최고가를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "입찰 현황 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 경매"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{auctionId}/bid")
    ResponseEntity<CommonResponse<BidStatusDto>> getAllBids(
        @PathVariable("auctionId") Long auctionId);
}
