package FreshBid.back.service.impl;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Auction.Status;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Live.LiveStatus;
import FreshBid.back.entity.Product;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.AuctionRepository;
import FreshBid.back.repository.AuctionRepositorySupport;
import FreshBid.back.repository.LiveRepository;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.ProductRepositorySupport;
import FreshBid.back.service.AuctionService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final LiveRepository liveRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AuctionRepositorySupport auctionRepositorySupport;
    private final ProductRepositorySupport productRepositorySupport;

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> searchAuctions(AuctionSearchRequestDto searchRequest) {
        log.info("경매 검색 시작 - 페이지: {}, 크기: {}, 정렬: {} {}",
            searchRequest.getPage(), searchRequest.getSize(),
            searchRequest.getSortBy(), searchRequest.getSortDirection());

        // Repository에서 검색 수행
        Page<Auction> auctionPage = auctionRepositorySupport.searchAuctions(searchRequest);

        // Auction 엔티티를 AuctionSearchResponseDto로 변환
        Page<AuctionResponseDto> result = auctionPage.map(AuctionResponseDto::from);

        log.info("경매 검색 완료 - 총 {}개 중 {}개 조회", auctionPage.getTotalElements(),
            auctionPage.getNumberOfElements());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionResponseDto getAuction(Long auctionId) {
        log.debug("경매 및 연관 엔티티 조회 시작 - 경매 ID: {}", auctionId);
        Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(auctionId);
        if (auction == null) {
            log.warn("경매 조회 실패 - 존재하지 않는 경매 ID: {}", auctionId);
            throw new NotFoundException("존재하지 않는 경매입니다: " + auctionId);
        }
        log.debug("경매 및 연관 엔티티 조회 완료 - 경매 ID: {}, 상품 ID: {}", auction.getId(),
            auction.getProduct().getId());
        return AuctionResponseDto.from(auction);
    }

    @Override
    @Transactional
    public void updateAuction(Long sellerId, Long auctionId, AuctionUpdateRequestDto request) {
        // 1. auctionId로 경매 및 상품 조회
        log.debug("경매 및 연관 엔티티 조회 시작 - 경매 ID: {}", auctionId);
        Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(auctionId);
        if (auction == null) {
            log.warn("경매 조회 실패 - 존재하지 않는 경매 ID: {}", auctionId);
            throw new NotFoundException("존재하지 않는 경매입니다: " + auctionId);
        }
        log.debug("경매 및 연관 엔티티 조회 완료 - 경매 ID: {}, 상품 ID: {}", auction.getId(),
            auction.getProduct().getId());

        // 2. 판매자 일치 검증
        log.debug("경매 검증 시작 - 경매 ID: {}, 판매자 ID: {}", auction.getId(),
            auction.getProduct().getUser().getId());
        if (!sellerId.equals(auction.getProduct().getUser().getId())) {
            log.warn("경매 판매자 ID 불일치 - 경매 ID: {}, 요청 판매자: {}, 실제 판매자: {}",
                auction.getId(), sellerId, auction.getProduct().getUser().getId());
            throw new ForbiddenException("경매 상품 판매자가 일치하지 않습니다");
        }

        // 3. 경매 수정 데이터 검증
        validateAuctionRequest(request, sellerId);

        // 4. 새로운 정보로 경매 수정
        auction.setStartPrice(
            request.getStartPrice() == 0 ? auction.getStartPrice() : request.getStartPrice());
        auction.setAmount(request.getAmount() == 0 ? auction.getAmount() : request.getAmount());

        // 상태 업데이트 (null이면 무시)
        Optional.ofNullable(request.getStatus()).ifPresent(auction::setStatus);

        auctionRepository.save(auction);

        log.info("경매 수정 완료 - 경매 ID: {}", auction.getId());
    }

    @Override
    @Transactional
    public void deleteAuction(Long sellerId, Long auctionId) {
        // 1. auctionId로 경매 및 상품 조회
        log.debug("경매 및 연관 엔티티 조회 시작 - 경매 ID: {}", auctionId);
        Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(auctionId);
        if (auction == null) {
            log.warn("경매 조회 실패 - 존재하지 않는 경매 ID: {}", auctionId);
            throw new NotFoundException("존재하지 않는 경매입니다: " + auctionId);
        }
        log.debug("경매 및 연관 엔티티 조회 완료 - 경매 ID: {}, 상품 ID: {}", auction.getId(),
            auction.getProduct().getId());

        // 2. 판매자 일치 검증
        log.debug("경매 검증 시작 - 경매 ID: {}, 판매자 ID: {}", auction.getId(),
            auction.getProduct().getUser().getId());
        if (!sellerId.equals(auction.getProduct().getUser().getId())) {
            log.warn("경매 판매자 ID 불일치 - 경매 ID: {}, 요청 판매자: {}, 실제 판매자: {}",
                auction.getId(), sellerId, auction.getProduct().getUser().getId());
            throw new ForbiddenException("경매 상품 판매자가 일치하지 않습니다");
        }

        // 3. 삭제
        auctionRepository.delete(auction);

        log.info("경매 삭제 완료 - 경매 ID: {}", auction.getId());
    }

    @Override
    public Product validateAuctionRequest(AuctionRequestDto auctionRequestDto, Long sellerId) {
        log.debug("경매 생성 검증 시작 - 상품 ID: {}, 판매자 ID: {}", auctionRequestDto.getProductId(),
            sellerId);

        // 1. 시작 가격 검증
        if (auctionRequestDto.getStartPrice() <= 0) {
            log.warn("경매 시작 가격 검증 실패 - 시작 가격: {}", auctionRequestDto.getStartPrice());
            throw new IllegalArgumentException("시작 가격은 0보다 커야 합니다");
        }

        // 2. 수량 검증
        if (auctionRequestDto.getAmount() <= 0) {
            log.warn("경매 수량 검증 실패 - 수량: {}", auctionRequestDto.getAmount());
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }
        // 3. 상품 ID 검증
        if (auctionRequestDto.getProductId() <= 0) {
            log.warn("경매 상품 ID 검증 실패 - 상품 ID: {}", auctionRequestDto.getProductId());
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
        }

        // 4. 상품 검증 및 조회
        log.debug("상품 조회 시작 - 상품 ID: {}", auctionRequestDto.getProductId());
        Product product = productRepository.findById(auctionRequestDto.getProductId())
            .orElseThrow(() -> {
                log.warn("존재하지 않는 상품 - 상품 ID: {}", auctionRequestDto.getProductId());
                return new NotFoundException("존재하지 않는 상품입니다");
            });
        // 5. 삭제된 상품 검증
        if (product.isDeleted()) {
            log.warn("판매 불가능한 상품 - 상품 ID: {}, 삭제 상태: {}", product.getId(), product.isDeleted());
            throw new IllegalArgumentException("판매 불가능한 상품입니다");
        }
        // 6. 상품 판매자 일치 검증
        if (!sellerId.equals(product.getUser().getId())) {
            log.warn("상품 판매자 불일치 - 상품 ID: {}, 요청 판매자: {}, 실제 판매자: {}",
                product.getId(), sellerId, product.getUser().getId());
            throw new ForbiddenException("상품 판매자가 일치하지 않습니다");
        }
        log.debug("경매 생성 검증 완료 - 상품 ID: {}, 판매자 ID: {}", auctionRequestDto.getProductId(),
            sellerId);
        return product;
    }

    @Override
    public void validateAuctionRequest(AuctionUpdateRequestDto auctionUpdateRequestDto,
        Long sellerId) {
        log.debug("경매 수정 검증 시작 - 판매자 ID: {}", sellerId);

        // 1. 시작 가격 검증
        if (auctionUpdateRequestDto.getStartPrice() <= 0) {
            log.warn("경매 시작 가격 검증 실패 - 시작 가격: {}", auctionUpdateRequestDto.getStartPrice());
            throw new IllegalArgumentException("시작 가격은 0보다 커야 합니다");
        }

        // 2. 수량 검증
        if (auctionUpdateRequestDto.getAmount() <= 0) {
            log.warn("경매 수량 검증 실패 - 수량: {}", auctionUpdateRequestDto.getAmount());
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }

        log.debug("경매 수정 검증 완료 - 판매자 ID: {}", sellerId);
    }

    @Override
    public void createAuction(AuctionRequestDto auctionRequestDto, Product product, Live live) {
        createAuctionInternal(auctionRequestDto, product, live);
    }

    @Override
    public AuctionResponseDto createAuctionByLiveId(AuctionRequestDto auctionRequestDto,
        Long liveId, Long currentUserId) {
        Live live = liveRepository.findById(liveId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 Live입니다."));
        if (Boolean.TRUE.equals(live.getIsDeleted())) {
            throw new NotFoundException("삭제된 Live입니다.");
        }
        if (!live.getSeller().getId().equals(currentUserId)) {
            throw new ForbiddenException("Live 판매자와 일치하지 않습니다.");
        }
        if (live.getStatus().equals(LiveStatus.ENDED)) {
            throw new IllegalArgumentException("종료된 Live에는 경매를 추가할 수 없습니다.");
        }

        Product product = validateAuctionRequest(auctionRequestDto, currentUserId);

        Auction auction = createAuctionInternal(auctionRequestDto, product, live);
        return AuctionResponseDto.from(auction);
    }

    @Override
    public void validateAndChangeAuctionStatus(Status asIs, Status toBe, Long sellerId,
        Long auctionId) {
        // 1. auctionId로 경매 및 상품 조회
        log.debug("경매 상태 변경 시작 - 경매 ID: {}, 예상 상태: {}, 변경할 상태: {}", auctionId, asIs, toBe);
        Auction auction = auctionRepositorySupport.findByIdWithProductsAndUser(auctionId);
        if (auction == null) {
            log.warn("경매 조회 실패 - 존재하지 않는 경매 ID: {}", auctionId);
            throw new NotFoundException("존재하지 않는 경매입니다: " + auctionId);
        }
        log.debug("경매 및 연관 엔티티 조회 완료 - 경매 ID: {}, 상품 ID: {}", auction.getId(),
            auction.getProduct().getId());

        // 2. 판매자 일치 검증
        log.debug("경매 검증 시작 - 경매 ID: {}, 판매자 ID: {}", auction.getId(),
            auction.getProduct().getUser().getId());
        if (!sellerId.equals(auction.getProduct().getUser().getId())) {
            log.warn("경매 판매자 ID 불일치 - 경매 ID: {}, 요청 판매자: {}, 실제 판매자: {}",
                auction.getId(), sellerId, auction.getProduct().getUser().getId());
            throw new ForbiddenException("경매 상품 판매자가 일치하지 않습니다");
        }

        // 3. 경매 상태 변경 데이터 검증
        log.debug("경매 상태 검증 - 경매 ID: {}, 예상 현재 상태: {}", auction.getId(), asIs);
        if (!auction.getStatus().equals(asIs)) {
            log.warn("경매 상태 불일치 - 경매 ID: {}, 실제 상태: {}, 예상 상태: {}", auction.getId(),
                auction.getStatus(), asIs);
            throw new ForbiddenException(
                "현재 경매 상태에서 진행할 수 없는 작업입니다. 현재 상태: " + auction.getStatus().toString());
        }

        // 4. 새로운 상태로 경매 수정
        auction.setStatus(toBe);

        auctionRepository.save(auction);

        log.info("경매 상태 변경 완료 - 경매 ID: {}", auction.getId());
    }

    private Auction createAuctionInternal(AuctionRequestDto auctionRequestDto, Product product,
        Live live) {
        log.debug("경매 생성 시작 - 상품 ID: {}, Live ID: {}", product.getId(), live.getId());
        Auction auction = Auction.of(auctionRequestDto, product, live);
        auctionRepository.save(auction);
        live.addAuction(auction);
        log.debug("경매 생성 완료 - 상품 ID: {}, Live ID: {}", product.getId(), live.getId());
        return auction;
    }
}
