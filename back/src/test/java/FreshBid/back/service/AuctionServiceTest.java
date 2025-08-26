package FreshBid.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.AuctionRepository;
import FreshBid.back.repository.AuctionRepositorySupport;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.ProductRepositorySupport;
import FreshBid.back.service.impl.AuctionServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("경매 서비스 테스트")
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuctionRepositorySupport auctionRepositorySupport;

    @Mock
    private ProductRepositorySupport productRepositorySupport;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private User testSeller;
    private User otherSeller;
    private ProductCategory testCategory;
    private Product testProduct;
    private Product deletedProduct;
    private Auction testAuction;
    private Live testLive;
    private AuctionRequestDto auctionRequestDto;
    private AuctionUpdateRequestDto auctionUpdateRequestDto;
    private AuctionSearchRequestDto auctionSearchRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testSeller = createTestUser(1L, "testseller");
        otherSeller = createTestUser(2L, "otherseller");

        // 테스트 카테고리 생성
        testCategory = ProductCategory.builder()
            .name("과일류")
            .build();

        // 테스트 상품 생성
        testProduct = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("홍로 사과")
            .origin("경북 예천")
            .weight(BigDecimal.valueOf(5.0))
            .description("당도 높은 신선한 홍로 사과입니다.")
            .build();
        testProduct.setId(1L);

        // 삭제된 상품 생성
        deletedProduct = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("삭제된 상품")
            .origin("경북 예천")
            .weight(BigDecimal.valueOf(5.0))
            .description("삭제된 상품입니다.")
            .build();
        deletedProduct.setId(2L);
        deletedProduct.setDeleted(true);

        // 테스트 라이브 생성
        testLive = new Live();
        testLive.setTitle("신선한 사과 경매");
        testLive.setStartDate(LocalDateTime.of(2025, 7, 25, 14, 0));
        testLive.setEndDate(LocalDateTime.of(2025, 7, 25, 16, 0));
        testLive.setSeller(testSeller);
        testLive.setId(1L);

        // 테스트 경매 생성
        testAuction = new Auction();
        testAuction.setId(1L);
        testAuction.setProduct(testProduct);
        testAuction.setLive(testLive);
        testAuction.setStartPrice(10000L);
        testAuction.setAmount(10);
        testAuction.setStatus(Auction.Status.SCHEDULED);

        // 경매 생성 요청 DTO
        auctionRequestDto = new AuctionRequestDto();
        auctionRequestDto.setProductId(1L);
        auctionRequestDto.setStartPrice(10000L);
        auctionRequestDto.setAmount(10);

        // 경매 수정 요청 DTO
        auctionUpdateRequestDto = new AuctionUpdateRequestDto();
        auctionUpdateRequestDto.setStartPrice(15000L);
        auctionUpdateRequestDto.setAmount(15);
        auctionUpdateRequestDto.setStatus(Auction.Status.IN_PROGRESS);

        // 경매 검색 요청 DTO
        auctionSearchRequestDto = new AuctionSearchRequestDto();
        auctionSearchRequestDto.setPage(0);
        auctionSearchRequestDto.setSize(20);
        auctionSearchRequestDto.setSortBy("startPrice");
        auctionSearchRequestDto.setSortDirection("ASC");
    }

    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_SELLER);
        user.setNickname("테스트판매자");
        user.setEmail("seller@test.com");
        user.setPhoneNumber("010-1234-5678");
        user.setIntroduction("신선한 농산물을 판매합니다");
        user.setAccountNumber("123-456-789");
        user.setAddress("경기도 수원시");
        return user;
    }

    @Test
    @DisplayName("경매 검색 성공")
    void searchAuctions_Success() {
        // given
        List<Auction> auctionList = Arrays.asList(testAuction);
        Page<Auction> auctionPage = new PageImpl<>(auctionList);
        given(auctionRepositorySupport.searchAuctions(auctionSearchRequestDto)).willReturn(
            auctionPage);

        // when
        Page<AuctionResponseDto> result = auctionService.searchAuctions(auctionSearchRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStartPrice()).isEqualTo(10000L);
        then(auctionRepositorySupport).should().searchAuctions(auctionSearchRequestDto);
    }

    @Test
    @DisplayName("경매 조회 성공")
    void getAuction_Success() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);

        // when
        AuctionResponseDto result = auctionService.getAuction(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStartPrice()).isEqualTo(10000L);
        then(auctionRepositorySupport).should().findByIdWithProductsAndUser(1L);
    }

    @Test
    @DisplayName("경매 조회 실패 - 존재하지 않는 경매")
    void getAuction_NotFound() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> auctionService.getAuction(999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 경매입니다: 999");
    }

    @Test
    @DisplayName("경매 수정 성공")
    void updateAuction_Success() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);
        given(auctionRepository.save(any(Auction.class))).willReturn(testAuction);

        // when
        auctionService.updateAuction(testSeller.getId(), 1L, auctionUpdateRequestDto);

        // then
        then(auctionRepositorySupport).should().findByIdWithProductsAndUser(1L);
        then(auctionRepository).should().save(testAuction);
        assertThat(testAuction.getStartPrice()).isEqualTo(15000L);
        assertThat(testAuction.getAmount()).isEqualTo(15);
    }

    @Test
    @DisplayName("경매 수정 실패 - 존재하지 않는 경매")
    void updateAuction_NotFound() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(
            () -> auctionService.updateAuction(testSeller.getId(), 999L, auctionUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 경매입니다: 999");

        then(auctionRepository).should(never()).save(any(Auction.class));
    }

    @Test
    @DisplayName("경매 수정 실패 - 권한 없는 사용자")
    void updateAuction_Forbidden() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);

        // when & then
        assertThatThrownBy(
            () -> auctionService.updateAuction(otherSeller.getId(), 1L, auctionUpdateRequestDto))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("경매 상품 판매자가 일치하지 않습니다");

        then(auctionRepository).should(never()).save(any(Auction.class));
    }

    @Test
    @DisplayName("경매 수정 실패 - 잘못된 시작 가격")
    void updateAuction_InvalidStartPrice() {
        // given
        auctionUpdateRequestDto.setStartPrice(-1000L);
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);

        // when & then
        assertThatThrownBy(
            () -> auctionService.updateAuction(testSeller.getId(), 1L, auctionUpdateRequestDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 가격은 0보다 커야 합니다");

        then(auctionRepository).should(never()).save(any(Auction.class));
    }

    @Test
    @DisplayName("경매 수정 실패 - 잘못된 수량")
    void updateAuction_InvalidAmount() {
        // given
        auctionUpdateRequestDto.setAmount(-5);
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);

        // when & then
        assertThatThrownBy(
            () -> auctionService.updateAuction(testSeller.getId(), 1L, auctionUpdateRequestDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("수량은 0보다 커야 합니다");

        then(auctionRepository).should(never()).save(any(Auction.class));
    }

    @Test
    @DisplayName("경매 삭제 성공")
    void deleteAuction_Success() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);
        doNothing().when(auctionRepository).delete(testAuction);

        // when
        auctionService.deleteAuction(testSeller.getId(), 1L);

        // then
        then(auctionRepositorySupport).should().findByIdWithProductsAndUser(1L);
        then(auctionRepository).should().delete(testAuction);
    }

    @Test
    @DisplayName("경매 삭제 실패 - 존재하지 않는 경매")
    void deleteAuction_NotFound() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> auctionService.deleteAuction(testSeller.getId(), 999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 경매입니다: 999");

        then(auctionRepository).should(never()).delete(any(Auction.class));
    }

    @Test
    @DisplayName("경매 삭제 실패 - 권한 없는 사용자")
    void deleteAuction_Forbidden() {
        // given
        given(auctionRepositorySupport.findByIdWithProductsAndUser(1L)).willReturn(testAuction);

        // when & then
        assertThatThrownBy(() -> auctionService.deleteAuction(otherSeller.getId(), 1L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("경매 상품 판매자가 일치하지 않습니다");

        then(auctionRepository).should(never()).delete(any(Auction.class));
    }

    @Test
    @DisplayName("경매 생성 검증 성공")
    void validateAuctionRequest_Success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // when
        Product result = auctionService.validateAuctionRequest(auctionRequestDto,
            testSeller.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("홍로 사과");
        then(productRepository).should().findById(1L);
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 잘못된 시작 가격")
    void validateAuctionRequest_InvalidStartPrice() {
        // given
        auctionRequestDto.setStartPrice(-1000L);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 가격은 0보다 커야 합니다");

        then(productRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 잘못된 수량")
    void validateAuctionRequest_InvalidAmount() {
        // given
        auctionRequestDto.setAmount(-5);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("수량은 0보다 커야 합니다");

        then(productRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 잘못된 상품 ID")
    void validateAuctionRequest_InvalidProductId() {
        // given
        auctionRequestDto.setProductId(-1L);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않은 상품 ID입니다");

        then(productRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 존재하지 않는 상품")
    void validateAuctionRequest_ProductNotFound() {
        // given
        given(productRepository.findById(999L)).willReturn(Optional.empty());
        auctionRequestDto.setProductId(999L);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, testSeller.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 상품입니다");
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 삭제된 상품")
    void validateAuctionRequest_DeletedProduct() {
        // given
        given(productRepository.findById(2L)).willReturn(Optional.of(deletedProduct));
        auctionRequestDto.setProductId(2L);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("판매 불가능한 상품입니다");
    }

    @Test
    @DisplayName("경매 생성 검증 실패 - 상품 판매자 불일치")
    void validateAuctionRequest_SellerMismatch() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionRequestDto, otherSeller.getId()))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("상품 판매자가 일치하지 않습니다");
    }

    @Test
    @DisplayName("경매 생성 성공")
    void createAuction_Success() {
        // given
        given(auctionRepository.save(any(Auction.class))).willReturn(testAuction);

        // when
        auctionService.createAuction(auctionRequestDto, testProduct, testLive);

        // then
        then(auctionRepository).should().save(any(Auction.class));
    }

    @Test
    @DisplayName("경매 수정 요청 검증 성공")
    void validateAuctionUpdateRequest_Success() {
        // when & then
        auctionService.validateAuctionRequest(auctionUpdateRequestDto, testSeller.getId());

        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("경매 수정 요청 검증 실패 - 잘못된 시작 가격")
    void validateAuctionUpdateRequest_InvalidStartPrice() {
        // given
        auctionUpdateRequestDto.setStartPrice(-1000L);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionUpdateRequestDto,
                testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("시작 가격은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("경매 수정 요청 검증 실패 - 잘못된 수량")
    void validateAuctionUpdateRequest_InvalidAmount() {
        // given
        auctionUpdateRequestDto.setAmount(-5);

        // when & then
        assertThatThrownBy(
            () -> auctionService.validateAuctionRequest(auctionUpdateRequestDto,
                testSeller.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("수량은 0보다 커야 합니다");
    }
}