package FreshBid.back.service;

import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.entity.User;
import org.springframework.data.domain.Page;

public interface LiveService {

    /**
     * Live와 연관된 Auction들을 함께 생성
     */
    LiveResponseDto createLiveWithAuctions(LiveCreateRequestDto liveCreateRequestDto, User seller);

    /**
     * Live 조회 (Auction 포함)
     */
    LiveResponseDto getLiveWithDetails(Long liveId);

    /**
     * Live 검색 및 페이징 조회
     */
    Page<LiveResponseDto> searchLives(LiveSearchRequestDto searchRequest);

    /**
     * Live 수정
     */
    void updateLive(Long sellerId, Long liveId, LiveUpdateRequestDto liveUpdateRequestDto);

    /**
     * Live 삭제
     *
     * @param sellerId 요청한 유저 ID (판매자 ID와 일치 필요)
     * @param liveId   삭제할 live ID
     */
    void deleteLive(Long sellerId, Long liveId);

    /**
     *  Live 소유자 검사 (소유자 아닌 경우 exception)
     */
    boolean checkOwner(Long sellerId, Long liveId);
}