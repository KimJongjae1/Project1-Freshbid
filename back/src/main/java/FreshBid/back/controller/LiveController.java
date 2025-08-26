package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Live 및 경매 API 인터페이스
 */
@Tag(name = "Live 관리 API", description = "Live(경매 포함) 등록, 조회, 수정, 삭제 관련 API")
@RequestMapping("/auction/live")
public interface LiveController {

    /**
     * Live 및 경매 등록
     *
     * @param liveCreateRequestDto Live 및 경매 등록 요청 데이터
     * @return 등록된 Live 및 경매 정보
     */
    @Operation(summary = "Live 및 경매 등록", description = "Live 및 Live에서 진행할 경매 정보를 등록합니다. 등록된 Live, 경매, 상품 정보를 반환합니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Live 및 경매 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (날짜, 가격, 수량, 상품 ID, 판매자 불일치 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") })
    @PostMapping("")
    ResponseEntity<CommonResponse<LiveResponseDto>> registerLive(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @RequestBody LiveCreateRequestDto liveCreateRequestDto) throws JsonProcessingException;

    /**
     * Live 검색 및 페이징 조회
     *
     * @param searchRequest 검색 조건을 담은 DTO
     * @return 페이징된 Live 검색 결과
     */
    @Operation(summary = "Live 검색 및 페이징 조회", description = "다양한 조건으로 Live를 검색하고 페이징된 결과를 반환합니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Live 검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") })
    @GetMapping("")
    ResponseEntity<CommonResponse<Page<LiveResponseDto>>> getAllLives(
            LiveSearchRequestDto searchRequest);

    /**
     * Live 정보 조회
     *
     * @param liveId 조회할 Live ID
     * @return Live 정보 및 연관된 경매 목록
     */
    @Operation(summary = "Live 정보 조회", description = "Live ID를 통해 Live 정보와 연관된 경매 목록을 조회합니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Live 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Live"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") })
    @GetMapping("/{liveId}")
    ResponseEntity<CommonResponse<LiveResponseDto>> getLive(@PathVariable("liveId") Long liveId);

    /**
     * Live 수정
     *
     * @param liveId               수정할 Live ID
     * @param liveUpdateRequestDto 수정사항을 담은 DTO
     * @return null
     */
    @Operation(summary = "Live 정보 수정", description = "이미 생성된 Live 정보를 수정합니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Live 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (날짜 검증 실패 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 Live가 아닌 경우)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Live"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") })
    @PutMapping("/{liveId}")
    ResponseEntity<CommonResponse<Void>> updateLive(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("liveId") Long liveId,
            @RequestBody LiveUpdateRequestDto liveUpdateRequestDto);

    /**
     * Live 삭제
     *
     * @param liveId 삭제할 Live ID
     * @return null
     */
    @Operation(summary = "Live 삭제", description = "Live를 삭제합니다. 관련된 경매가 함께 자동 제거됩니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Live 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 Live가 아닌 경우)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 Live"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") })
    @DeleteMapping("/{liveId}")
    ResponseEntity<CommonResponse<Void>> deleteLive(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable("liveId") Long liveId);

    /**
     * Live 소유 검사
     *
     * @param liveId  검사할 Live ID
     * @return boolean, true만 반환됨
     */
    @Operation(summary = "Live 소유자 검사", description = "현재 로그인한 사용자가 Live의 소유자인지 검사합니다.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Live와 소유자가 일치함"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "Live의 소유자가 일치하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 Live"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{liveId}/ownership")
    ResponseEntity<CommonResponse<Boolean>> checkLiveOwnership(
        @AuthenticationPrincipal FreshBidUserDetails userDetails,
        @PathVariable("liveId") Long liveId);
}
