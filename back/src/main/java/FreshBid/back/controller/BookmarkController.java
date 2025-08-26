package FreshBid.back.controller;

import FreshBid.back.dto.user.FreshBidUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "찜 CRUD", description = "회원(소비자)이 찜한 판매자와 라이브를 관리합니다.")
@RequestMapping("/bookmark")
public interface BookmarkController {

    @Operation(summary = "판매자 찜 목록 조회", description = "로그인 사용자가 찜한 판매자들 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 찜 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/seller")
    public ResponseEntity<?> getFavoriteSellers(@AuthenticationPrincipal FreshBidUserDetails userDetails);

    @Operation(summary = "판매자 찜 등록", description = "판매자 찜을 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 찜 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @PostMapping("/seller/{id}")
    public ResponseEntity<?> addFavoriteSeller(@AuthenticationPrincipal FreshBidUserDetails userDetails,
                                               @PathVariable("id") Long sellerId);

    @Operation(summary = "판매자 찜 삭제", description = "찜한 판매자를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 찜 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @DeleteMapping("/seller/{id}")
    public ResponseEntity<?> removeFavoriteSeller(@AuthenticationPrincipal FreshBidUserDetails userDetails,
                                               @PathVariable("id") Long sellerId);

    @Operation(summary = "경매 라이브 찜 조회", description = "찜한 경매 라이브를 보여줍니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "찜한 경매 라이브 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/live")
    public ResponseEntity<?> getFavoriteAuctionLives(@AuthenticationPrincipal FreshBidUserDetails userDetails);

    @Operation(summary = "경매 라이브 찜", description = "경매 라이브를 찜합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경매 라이브 찜 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @PostMapping("/live/{id}")
    public ResponseEntity<?> addFavoriteAuctionLive(@AuthenticationPrincipal FreshBidUserDetails userDetails,
                                                    @PathVariable("id") Long liveId);

    @Operation(summary = "경매 라이브 찜 삭제", description = "찜 목록에서 라이브를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경매 라이브 찜 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @DeleteMapping("/live/{id}")
    public ResponseEntity<?> removeFavoriteAuctionLive(@AuthenticationPrincipal FreshBidUserDetails userDetails,
                                                       @PathVariable("id") Long liveId);
}
