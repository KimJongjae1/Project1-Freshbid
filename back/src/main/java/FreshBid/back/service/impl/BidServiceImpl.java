package FreshBid.back.service.impl;

import FreshBid.back.dto.bid.BidRequestDto;
import FreshBid.back.dto.bid.BidResponseDto;
import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.AuctionHistory;
import FreshBid.back.entity.BidRecord;
import FreshBid.back.entity.User;
import FreshBid.back.entity.User.Role;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.AuctionHistoryRepository;
import FreshBid.back.repository.AuctionRepository;
import FreshBid.back.repository.BidRedisRepositorySupport;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.service.BidService;
import FreshBid.back.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final OrderService orderService;
    private final BidRedisRepositorySupport bidRedisRepositorySupport;
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createBid(Long auctionId, User user, Long bidPrice) {
        BidRequestDto bidRequestDto = new BidRequestDto(bidPrice);
        createBid(auctionId, user, bidRequestDto);
    }

    @Override
    @Transactional
    public void createBid(Long auctionId, User user, BidRequestDto bidRequestDto) {
        log.info("입찰 생성 시작 - 경매 ID: {}, 사용자 ID: {}, 입찰가: {}",
            auctionId, user.getId(), bidRequestDto.getBidPrice());

        // 1. 경매 존재 및 상태 확인
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 경매입니다."));

        if (auction.getStatus() != Auction.Status.IN_PROGRESS) {
            throw new ForbiddenException("진행 중인 경매가 아닙니다.");
        }

        // 2. 현재 3초 구간의 최소 입찰가 확인
        Long currentMinBidPrice = getCurrentValidMinPrice(auctionId, auction.getStartPrice());

        if (bidRequestDto.getBidPrice() < currentMinBidPrice) {
            throw new IllegalArgumentException(
                "입찰가가 현재 최소 입찰가보다 높아야 합니다. 현재 최소 입찰가: " + currentMinBidPrice);
        }

        // 3. 입찰 레코드 생성 및 저장
        BidRecord bidRecord = BidRecord.createBid(auctionId, user.getId(), user.getNickname(),
            bidRequestDto.getBidPrice());
        bidRedisRepositorySupport.saveBid(bidRecord);
        String bidId = bidRecord.getBidId();

        log.info("입찰 생성 완료 - 입찰 ID: {}", bidId);
    }

    @Override
    @Transactional(readOnly = true)
    public BidStatusDto getBidStatus(Long auctionId, int limit) {
        log.info("입찰 현황 조회 시작 - 경매 ID: {}, 제한: {}", auctionId, limit);

        // 1. 경매 존재 확인
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 경매입니다."));

        // 2. Redis에서 입찰 목록 조회
        List<BidRecord> bidRecords;
        if (limit == 0) {
            bidRecords = bidRedisRepositorySupport.getAllBidsByAuctionId(auctionId);
        } else {
            bidRecords = bidRedisRepositorySupport.getTopBids(auctionId, limit);
        }
        // 3. 응답 DTO 생성
        List<BidResponseDto> bidList = bidRecords.stream()
            .map(this::convertToBidResponseDto)
            .toList();

        BidResponseDto highestBid = !bidList.isEmpty() ? bidList.get(0) : null;
        Long currentHighestPrice =
            highestBid != null ? highestBid.getBidPrice() : auction.getStartPrice();

        BidStatusDto response = BidStatusDto.builder()
            .auctionId(auctionId)
            .status(auction.getStatus().name())
            .currentHighestPrice(currentHighestPrice)
            .bidList(bidList)
            .highestBid(highestBid)
            .build();

        log.info("입찰 현황 조회 완료 - 조회한 입찰 수: {}, 최고가: {}", bidRecords.size(), currentHighestPrice);
        return response;
    }

    @Override
    @Transactional
    public BidResponseDto finalizeBid(Long auctionId) {
        log.info("경매 낙찰 처리 시작 - 경매 ID: {}", auctionId);

        // 1. 경매 존재 확인
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 경매입니다."));

        // 2. Redis에서 모든 입찰 데이터 조회
        List<BidRecord> bidRecords = bidRedisRepositorySupport.getAllBidsByAuctionId(auctionId);

        if (bidRecords.isEmpty()) {
            log.info("입찰이 없는 경매 - 경매 ID: {}", auctionId);
            return null;
        }

        // 3. 최고가 입찰자 식별
        BidRecord highestBid = bidRecords.get(0);
        
        // 4. 최고가 입찰자를 제외한 나머지 입찰 기록을 MySQL로 이관
        List<AuctionHistory> regularBids = bidRecords.stream()
            .skip(1) // 최고가 입찰자(첫 번째) 제외
            .map(AuctionHistory::fromBidRecord)
            .toList();
        if (!regularBids.isEmpty()) {
            auctionHistoryRepository.saveAll(regularBids);
        }

        // 5. 최고가 입찰자는 '낙찰' 액션으로 저장
        AuctionHistory winningBid = AuctionHistory.builder()
            .auctionId(auctionId)
            .userId(highestBid.getUserId())
            .action(AuctionHistory.Action.낙찰)
            .actionTime(highestBid.getBidTime())
            .price(highestBid.getBidPrice())
            .build();
        auctionHistoryRepository.save(winningBid);

        // 6. Redis 데이터 정리
        bidRedisRepositorySupport.deleteByAuctionId(auctionId);
        // Redis에서 경매-룸 매핑 및 최소 입찰가 삭제
        bidRedisRepositorySupport.removeActiveAuction(auctionId);
        bidRedisRepositorySupport.removeCurrentMinBidPrice(auctionId);

        // 7. 응답 생성
        BidResponseDto response = convertToBidResponseDto(highestBid);

        log.info("경매 낙찰 처리 완료 - 낙찰자: {}, 낙찰가: {}", highestBid.getUserNickName(),
            highestBid.getBidPrice());

        // 7. 주문 생성
        try {
            orderService.createOrder(winningBid);
            log.info("낙찰 경매 주문 생성 성공");
        } catch (RuntimeException e) {
            throw new RuntimeException("경매 주문 생성에 실패했습니다. " + e.getMessage());
        }

        return response;
    }

    private BidResponseDto convertToBidResponseDto(BidRecord bidRecord) {
        return BidResponseDto.builder()
            .bidId(Long.parseLong(bidRecord.getBidId().split(":")[2])) // timestamp 부분 사용
            .auctionId(bidRecord.getAuctionId())
            .userId(bidRecord.getUserId())
            .userNickName(bidRecord.getUserNickName())
            .bidPrice(bidRecord.getBidPrice())
            .bidTime(bidRecord.getBidTime())
            .build();
    }

    /**
     * 현재 3초 구간의 유효한 최소 입찰가 조회 Redis에 저장된 값이 없으면 경매 시작가를 반환
     */
    private Long getCurrentValidMinPrice(Long auctionId, Long startPrice) {
        Long currentMinBidPrice = bidRedisRepositorySupport.getCurrentMinBidPrice(auctionId);
        return currentMinBidPrice != null ? currentMinBidPrice : startPrice;
    }
}