package FreshBid.back.service.impl;

import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.entity.*;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.*;
import FreshBid.back.service.BookmarkService;
import FreshBid.back.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final SellerBookmarkRepository sellerBookmarkRepository;

    private final SellerBookmarkRepositorySupport sellerBookmarkRepositorySupport;

    private final LiveBookmarkRepository liveBookmarkRepository;

    private final LiveBookmarkRepositorySupport liveBookmarkRepositorySupport;

    private final LiveRepository liveRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;
    @Override
    @Transactional
    public void addLiveBookmark(Long liveId, User user) {
        Live targetLive = liveRepository.findById(liveId)
                .orElseThrow(() -> new NotFoundException("해당 아이디의 라이브가 존재하지 않습니다."));
        if (Boolean.TRUE.equals(targetLive.getIsDeleted())) {
            throw new NotFoundException("삭제된 Live입니다.");
        }

        LiveBookmark liveBookmark = LiveBookmark.of(targetLive, user);
        liveBookmarkRepository.save(liveBookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveBasicResponseDto> getLiveBookmarks(Long userId) {
        List<LiveBasicResponseDto> bookmarkedLives = liveBookmarkRepositorySupport.findByUserId(userId);

        //라이브 이미지 매핑
        for(LiveBasicResponseDto dto: bookmarkedLives) {
            String url = fileStorageService.getUrl(dto.getReprImgSrc());
            dto.setReprImgSrc(fileStorageService.convertImageUrlToBlob(url));
        }
        return bookmarkedLives;
    }

    @Override
    @Transactional
    public void deleteLiveBookmark(Long userId, Long liveId) {
        LiveBookmark bookmark = liveBookmarkRepository.findById(new LiveBookmarkKey(liveId, userId))
                .orElseThrow(() -> new NotFoundException("해당 아이디의 라이브 찜이 존재하지 않습니다."));

        liveBookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional
    public void addSellerBookmark(Long sellerId, User user) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("해당 아이디의 유저가 존재하지 않습니다."));
        //판매자 유저여야 함.
        if(seller.getRole() != User.Role.ROLE_SELLER) {
            throw new NotFoundException("해당 아이디의 판매자가 존재하지 않습니다.");
        }

        SellerBookmark sellerBookmark = SellerBookmark.of(seller, user);
        sellerBookmarkRepository.save(sellerBookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerBasicInfoDto> getSellerBookmarks(Long userId) {
        return sellerBookmarkRepositorySupport.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteSellerBookmark(Long sellerId, Long userId) {
        SellerBookmark sellerBookmark = sellerBookmarkRepository.findById(new SellerBookmarkKey(sellerId, userId))
                .orElseThrow(() -> new NotFoundException("해당 아이디의 판매자 찜이 존재하지 않습니다."));

        sellerBookmarkRepository.delete(sellerBookmark);
    }
}
