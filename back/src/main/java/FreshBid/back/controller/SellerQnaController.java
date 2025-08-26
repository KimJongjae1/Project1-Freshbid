package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.SellerQna.*;
import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매자 문의 API", description = "판매자 문의 등록, 조회, 수정, 삭제 관련 API")
@RequestMapping("/auction/qna")
public interface SellerQnaController {

    @Operation(summary = "문의 등록", description = "판매자에게 문의를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문의 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 판매자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    ResponseEntity<CommonResponse<Void>> createQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @RequestBody SellerQnaCreateRequestDto requestDto
    );

    @Operation(summary = "판매자 문의 전체 조회", description = "판매자 ID에 해당하는 문의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/seller/{sellerId}")
    ResponseEntity<CommonResponse<Page<SellerQnaResponseDto>>> getQnasBySeller(
            @PathVariable Long sellerId,
            SellerQnaSearchRequestDto searchDto
    );

    @Operation(summary = "문의 단건 조회", description = "특정 문의를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문의"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{qnaId}")
    ResponseEntity<CommonResponse<SellerQnaResponseDto>> getQnaById(
            @PathVariable Long qnaId
    );

    @Operation(summary = "문의 수정", description = "본인이 작성한 문의를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 문의 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문의"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{qnaId}")
    ResponseEntity<CommonResponse<Void>> updateQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Long qnaId,
            @RequestBody SellerQnaUpdateRequestDto requestDto
    );

    @Operation(summary = "문의 삭제", description = "본인이 작성한 문의를 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 문의 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문의"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{qnaId}")
    ResponseEntity<CommonResponse<Void>> deleteQna(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @PathVariable Long qnaId
    );
} 