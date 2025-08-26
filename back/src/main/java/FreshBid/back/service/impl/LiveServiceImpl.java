package FreshBid.back.service.impl;

import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.dto.live.SellerResponseDto;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.LiveRepository;
import FreshBid.back.repository.LiveRepositorySupport;
import FreshBid.back.service.AuctionService;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.LiveService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveServiceImpl implements LiveService {

    private final LiveRepository liveRepository;
    private final LiveRepositorySupport liveRepositorySupport;
    private final AuctionService auctionService;
    private final FileStorageService fileStorageService;
    private static final String MINIO_PREFIX = "live";
    @Override
    @Transactional
    public LiveResponseDto createLiveWithAuctions(LiveCreateRequestDto liveCreateRequestDto,
        User seller) {
        log.info("Live 및 경매 생성 시작 - 판매자 ID: {}, 경매 수: {}", seller.getId(),
            liveCreateRequestDto.getAuctions().size());
        // Product 임시 저장할 맵 생성
        log.debug("Product 임시 저장용 맵 생성");
        Map<Long, Product> productMap = new HashMap<>();

        // 1. Live에 대한 검증
        log.debug("Live 날짜 검증 시작 - 시작일: {}, 종료일: {}", liveCreateRequestDto.getStartDate(),
            liveCreateRequestDto.getEndDate());
        if (liveCreateRequestDto.getStartDate().isAfter(liveCreateRequestDto.getEndDate())) {
            log.warn("Live 날짜 검증 실패 - 종료 날짜가 시작 날짜보다 이전입니다");
            throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다");
        }

        // 2. 각 Auction에 대한 검증
        log.debug("경매 데이터 검증 시작 - 총 {} 개 경매", liveCreateRequestDto.getAuctions().size());
        liveCreateRequestDto.getAuctions().forEach(auctionRequestDto -> {
            productMap.put(auctionRequestDto.getProductId(),
                auctionService.validateAuctionRequest(auctionRequestDto, seller.getId()));
        });

        // 3. Live 생성
        log.debug("Live 엔티티 생성 및 저장 시작");
        Live live = Live.of(liveCreateRequestDto);
        live.setSeller(seller);

        //라이브 이미지 저장
        String filePath = fileStorageService.uploadImage(MINIO_PREFIX, liveCreateRequestDto.getImgFile());
        live.setReprImgSrc(filePath);

        liveRepository.save(live);
        log.info("Live 생성 완료 - Live ID: {}", live.getId());

        // 4. Auction 생성
        log.debug("Auction 생성 시작 - 총 {} 개 경매", liveCreateRequestDto.getAuctions().size());
        liveCreateRequestDto.getAuctions().forEach(auctionRequestDto -> {
            Product product = productMap.get(auctionRequestDto.getProductId());
            auctionService.createAuction(auctionRequestDto, product, live);
        });
        log.info("Live 및 경매 생성 완료 - Live ID: {}, 판매자 ID: {}", live.getId(), seller.getId());
        return LiveResponseDto.from(live);
    }

    @Override
    @Transactional(readOnly = true)
    public LiveResponseDto getLiveWithDetails(Long liveId) {
        log.info("Live 조회 시작 - Live ID: {}", liveId);
        log.debug("Live 및 연관 엔티티 조회 시작 - Live ID: {}", liveId);
        Live live = liveRepositorySupport.findByIdWithAuctionsAndProducts(liveId);
        if (live == null) {
            log.warn("Live 조회 실패 - 존재하지 않는 Live ID: {}", liveId);
            throw new NotFoundException("존재하지 않는 Live입니다: " + liveId);
        }
        log.debug("Live 및 연관 엔티티 조회 완료 - Live ID: {}, Auction 수: {}", live.getId(),
            live.getAuctions().size());
        return LiveResponseDto.from(live);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LiveResponseDto> searchLives(LiveSearchRequestDto searchRequest) {
        log.info("Live 검색 시작 - 페이지: {}, 크기: {}, 정렬: {} {}",
            searchRequest.getPage(), searchRequest.getSize(),
            searchRequest.getSortBy(), searchRequest.getSortDirection());

        // Repository에서 검색 수행
        Page<Live> livePage = liveRepositorySupport.searchLives(searchRequest);

        // Live 엔티티를 LiveSearchResponseDto로 변환
        Page<LiveResponseDto> result = livePage.map(LiveResponseDto::from);

        //blob 이미지 추가
        for(LiveResponseDto dto: result.getContent()) {
            String base64img = fileStorageService.convertImageUrlToBlob(fileStorageService.getUrl(dto.getReprImgSrc()));
            dto.setReprImgSrc(base64img);
        }

        log.info("Live 검색 완료 - 총 {}개 중 {}개 조회", livePage.getTotalElements(),
            livePage.getNumberOfElements());
        return result;
    }

    /**
     * Live에서 판매자 정보를 수집하는 메서드
     */
    private List<SellerResponseDto> collectSellersFromLive(Live live) {
        Set<User> sellers = live.getAuctions().stream()
            .map(auction -> auction.getProduct().getUser())
            .collect(Collectors.toSet());

        return sellers.stream()
            .map(SellerResponseDto::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateLive(Long sellerId, Long liveId, LiveUpdateRequestDto liveUpdateRequestDto) {
        log.info("Live 수정 시작 - Live ID: {}, 요청 데이터: {}", liveId, liveUpdateRequestDto);

        Live live = getLive(sellerId, liveId);

        // 3. 새로운 정보로 live 수정 및 null이면 무시
        Optional.ofNullable(liveUpdateRequestDto.getTitle()).ifPresent(live::setTitle);
        Optional.ofNullable(liveUpdateRequestDto.getStartDate()).ifPresent(live::setStartDate);
        Optional.ofNullable(liveUpdateRequestDto.getEndDate()).ifPresent(live::setEndDate);
        Optional.ofNullable(liveUpdateRequestDto.getStatus()).ifPresent(live::setStatus);

        // 4. live 날짜 검증
        log.debug("Live 날짜 검증 시작 - 시작일: {}, 종료일: {}", live.getStartDate(), live.getEndDate());
        if (live.getStartDate().isAfter(live.getEndDate())) {
            log.warn("Live 날짜 검증 실패 - 종료 날짜가 시작 날짜보다 이전입니다");
            throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다");
        }

        liveRepository.save(live);

        log.info("Live 수정 완료 - Live ID: {}", live.getId());
    }

    @Override
    @Transactional
    public void deleteLive(Long sellerId, Long liveId) {
        log.info("Live 삭제 시작 - Live ID: {}", liveId);

        Live live = getLive(sellerId, liveId);
        live.setIsDeleted(Boolean.TRUE);

        liveRepository.save(live);

        log.info("Live 삭제 완료 - Live ID: {}", live.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkOwner(Long sellerId, Long liveId) {
        log.info("Live 소유자 검사 시작 - Live ID: {}", liveId);
        getLive(sellerId, liveId);
        return true;
    }

    private Live getLive(Long sellerId, Long liveId) {
        log.debug("Live 조회 시작 - Live ID: {}, seller ID: {}", liveId, sellerId);

        // 1. liveId로 live 조회
        Live live = liveRepository.findById(liveId)
            .orElseThrow(() -> {
                log.warn("존재하지 않는 Live - Live ID: {}", liveId);
                return new NotFoundException("존재하지 않는 Live입니다");
            });
        if (Boolean.TRUE.equals(live.getIsDeleted())) {
            throw new NotFoundException("삭제된 Live입니다.");
        }

        // 2. 판매자 일치 검증
        log.debug("Live 검증 시작 - Live ID: {}, 판매자 ID: {}", live.getId(), live.getSeller().getId());
        if (!sellerId.equals(live.getSeller().getId())) {
            log.warn("Live 판매자 ID 불일치 - Live ID: {}, 요청 판매자: {}, 실제 판매자: {}",
                live.getId(), sellerId, live.getSeller().getId());
            throw new ForbiddenException("Live 판매자가 일치하지 않습니다");
        }

        return live;
    }

    //endDate가 지난 live들 ENDED로 변경
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    @Transactional
    public void checkEndedLives() {
        LocalDateTime now = LocalDateTime.now();

        List<Live> expiredLives = liveRepositorySupport.findExpiredLives(now);
        for(Live live: expiredLives) {
            live.setStatus(Live.LiveStatus.ENDED);
        }
    }
}
