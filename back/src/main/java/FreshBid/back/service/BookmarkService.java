package FreshBid.back.service;

import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.entity.User;

import java.util.List;

public interface BookmarkService {

    public void addLiveBookmark(Long liveId, User user);

    public List<LiveBasicResponseDto> getLiveBookmarks(Long userId);

    public void deleteLiveBookmark(Long userId, Long liveId);

    public void addSellerBookmark(Long sellerId, User user);

    public List<SellerBasicInfoDto> getSellerBookmarks(Long userId);

    public void deleteSellerBookmark(Long sellerId, Long userId);
}
