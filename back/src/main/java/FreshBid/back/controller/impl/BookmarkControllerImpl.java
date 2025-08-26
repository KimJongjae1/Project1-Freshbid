package FreshBid.back.controller.impl;

import FreshBid.back.controller.BookmarkController;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.entity.User;
import FreshBid.back.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookmarkControllerImpl implements BookmarkController {

    private final BookmarkService bookmarkService;
    @Override
    public ResponseEntity<?> getFavoriteSellers(FreshBidUserDetails userDetails) {
        Long currentUserId = userDetails.getUser().getId();

        List<SellerBasicInfoDto> sellerBookmarks = bookmarkService.getSellerBookmarks(currentUserId);
        CommonResponse<List<SellerBasicInfoDto>> response
                = CommonResponse.<List<SellerBasicInfoDto>>builder()
                .success(true).message("찜한 판매자 조회 완료")
                .data(sellerBookmarks).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> addFavoriteSeller(FreshBidUserDetails userDetails, Long sellerId) {
        User currentUser = userDetails.getUser();

        bookmarkService.addSellerBookmark(sellerId, currentUser);
        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true).message("판매자 찜 성공")
                .data(null).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> removeFavoriteSeller(FreshBidUserDetails userDetails, Long sellerId) {
        Long currentUserId = userDetails.getUser().getId();
        bookmarkService.deleteSellerBookmark(sellerId, currentUserId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true).message("판매자 찜 삭제 성공")
                .data(null).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getFavoriteAuctionLives(FreshBidUserDetails userDetails) {
        Long currentUserId = userDetails.getUser().getId();

        List<LiveBasicResponseDto> liveBookmarks = bookmarkService.getLiveBookmarks(currentUserId);
        CommonResponse<List<LiveBasicResponseDto>> response
                = CommonResponse.<List<LiveBasicResponseDto>>builder()
                .success(true).message("찜한 경매 라이브 조회 완료")
                .data(liveBookmarks).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> addFavoriteAuctionLive(FreshBidUserDetails userDetails, Long liveId) {
        User currentUser = userDetails.getUser();

        bookmarkService.addLiveBookmark(liveId, currentUser);
        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true).message("경매 라이브 찜 성공")
                .data(null).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> removeFavoriteAuctionLive(FreshBidUserDetails userDetails, Long liveId) {
        Long currentUserId = userDetails.getUser().getId();
        bookmarkService.deleteLiveBookmark(currentUserId, liveId);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .success(true).message("경매 라이브 찜 삭제 성공")
                .data(null).build();

        return ResponseEntity.ok(response);
    }
}
