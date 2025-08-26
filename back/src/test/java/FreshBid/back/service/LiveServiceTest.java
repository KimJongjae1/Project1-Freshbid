
package FreshBid.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.LiveRepository;
import FreshBid.back.repository.LiveRepositorySupport;
import FreshBid.back.service.impl.LiveServiceImpl;
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
@DisplayName("라이브 서비스 테스트")
class LiveServiceTest {

    @Mock
    private LiveRepository liveRepository;

    @Mock
    private LiveRepositorySupport liveRepositorySupport;

    @Mock
    private AuctionService auctionService;

    @InjectMocks
    private LiveServiceImpl liveService;

    private User testSeller;
    private User otherSeller;
    private ProductCategory testCategory;
    private Product testProduct;
    private Live testLive;
    private LiveCreateRequestDto liveCreateRequestDto;
    private LiveUpdateRequestDto liveUpdateRequestDto;
    private LiveSearchRequestDto liveSearchRequestDto;
    private AuctionRequestDto auctionRequestDto;

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

        // 테스트 라이브 생성
        testLive = new Live();
        testLive.setTitle("신선한 사과 경매");
        testLive.setStartDate(LocalDateTime.of(2025, 7, 25, 14, 0));
        testLive.setEndDate(LocalDateTime.of(2025, 7, 25, 16, 0));
        testLive.setSeller(testSeller);
        testLive.setId(1L);

        // 경매 요청 DTO
        auctionRequestDto = new AuctionRequestDto();
        auctionRequestDto.setProductId(1L);
        auctionRequestDto.setStartPrice(10000L);
        auctionRequestDto.setAmount(10);

        // 라이브 생성 요청 DTO
        liveCreateRequestDto = new LiveCreateRequestDto();
        liveCreateRequestDto.setTitle("신선한 사과 경매");
        liveCreateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 25, 14, 0));
        liveCreateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 25, 16, 0));
        liveCreateRequestDto.setAuctions(Arrays.asList(auctionRequestDto));

        // 라이브 수정 요청 DTO
        liveUpdateRequestDto = new LiveUpdateRequestDto();
        liveUpdateRequestDto.setTitle("수정된 사과 경매");
        liveUpdateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 26, 14, 0));
        liveUpdateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 26, 16, 0));

        // 라이브 검색 요청 DTO
        liveSearchRequestDto = new LiveSearchRequestDto();
        liveSearchRequestDto.setPage(0);
        liveSearchRequestDto.setSize(20);
        liveSearchRequestDto.setSortBy("startDate");
        liveSearchRequestDto.setSortDirection("ASC");
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
    @DisplayName("Live 및 경매 생성 성공")
    void createLiveWithAuctions_Success() {
        // given
        given(auctionService.validateAuctionRequest(eq(auctionRequestDto), eq(testSeller.getId())))
            .willReturn(testProduct);
        given(liveRepository.save(any(Live.class))).willReturn(testLive);
        doNothing().when(auctionService)
            .createAuction(eq(auctionRequestDto), eq(testProduct), any(Live.class));

        // when
        LiveResponseDto result = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("신선한 사과 경매");
        then(auctionService).should()
            .validateAuctionRequest(eq(auctionRequestDto), eq(testSeller.getId()));
        then(liveRepository).should().save(any(Live.class));
        then(auctionService).should()
            .createAuction(eq(auctionRequestDto), eq(testProduct), any(Live.class));
    }

    @Test
    @DisplayName("Live 생성 실패 - 잘못된 시간 설정")
    void createLiveWithAuctions_InvalidTimeRange() {
        // given
        liveCreateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 25, 16, 0));
        liveCreateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 25, 14, 0));

        // when & then
        assertThatThrownBy(
            () -> liveService.createLiveWithAuctions(liveCreateRequestDto, testSeller))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("종료 날짜는 시작 날짜 이후여야 합니다");

        then(liveRepository).should(never()).save(any(Live.class));
    }

    @Test
    @DisplayName("Live 조회 성공")
    void getLive_WithDetails_Success() {
        // given
        given(liveRepositorySupport.findByIdWithAuctionsAndProducts(1L)).willReturn(testLive);

        // when
        LiveResponseDto result = liveService.getLiveWithDetails(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("신선한 사과 경매");
        then(liveRepositorySupport).should().findByIdWithAuctionsAndProducts(1L);
    }

    @Test
    @DisplayName("Live 조회 실패 - 존재하지 않는 Live")
    void getLive_WithDetails_NotFound() {
        // given
        given(liveRepositorySupport.findByIdWithAuctionsAndProducts(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> liveService.getLiveWithDetails(999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 Live입니다: 999");
    }

    @Test
    @DisplayName("Live 검색 성공")
    void searchLives_Success() {
        // given
        List<Live> liveList = Arrays.asList(testLive);
        Page<Live> livePage = new PageImpl<>(liveList);
        given(liveRepositorySupport.searchLives(liveSearchRequestDto)).willReturn(livePage);

        // when
        Page<LiveResponseDto> result = liveService.searchLives(liveSearchRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("신선한 사과 경매");
        then(liveRepositorySupport).should().searchLives(liveSearchRequestDto);
    }

    @Test
    @DisplayName("Live 수정 성공")
    void updateLive_Success() {
        // given
        given(liveRepository.findById(1L)).willReturn(Optional.of(testLive));
        given(liveRepository.save(any(Live.class))).willReturn(testLive);

        // when
        liveService.updateLive(testSeller.getId(), 1L, liveUpdateRequestDto);

        // then
        then(liveRepository).should().findById(1L);
        then(liveRepository).should().save(testLive);
        assertThat(testLive.getTitle()).isEqualTo("수정된 사과 경매");
    }

    @Test
    @DisplayName("Live 수정 실패 - 존재하지 않는 Live")
    void updateLive_NotFound() {
        // given
        given(liveRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
            () -> liveService.updateLive(testSeller.getId(), 999L, liveUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 Live입니다");

        then(liveRepository).should(never()).save(any(Live.class));
    }

    @Test
    @DisplayName("Live 수정 실패 - 권한 없는 사용자")
    void updateLive_Forbidden() {
        // given
        given(liveRepository.findById(1L)).willReturn(Optional.of(testLive));

        // when & then
        assertThatThrownBy(
            () -> liveService.updateLive(otherSeller.getId(), 1L, liveUpdateRequestDto))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Live 판매자가 일치하지 않습니다");

        then(liveRepository).should(never()).save(any(Live.class));
    }

    @Test
    @DisplayName("Live 수정 실패 - 잘못된 시간 설정")
    void updateLive_InvalidTimeRange() {
        // given
        liveUpdateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 26, 16, 0));
        liveUpdateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 26, 14, 0));
        given(liveRepository.findById(1L)).willReturn(Optional.of(testLive));

        // when & then
        assertThatThrownBy(
            () -> liveService.updateLive(testSeller.getId(), 1L, liveUpdateRequestDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("종료 날짜는 시작 날짜 이후여야 합니다");

        then(liveRepository).should(never()).save(any(Live.class));
    }

    @Test
    @DisplayName("Live 삭제 성공")
    void deleteLive_Success() {
        // given
        given(liveRepository.findById(1L)).willReturn(Optional.of(testLive));
        doNothing().when(liveRepository).delete(testLive);

        // when
        liveService.deleteLive(testSeller.getId(), 1L);

        // then
        then(liveRepository).should().findById(1L);
        then(liveRepository).should().delete(testLive);
    }

    @Test
    @DisplayName("Live 삭제 실패 - 존재하지 않는 Live")
    void deleteLive_NotFound() {
        // given
        given(liveRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> liveService.deleteLive(testSeller.getId(), 999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 Live입니다");

        then(liveRepository).should(never()).delete(any(Live.class));
    }

    @Test
    @DisplayName("Live 삭제 실패 - 권한 없는 사용자")
    void deleteLive_Forbidden() {
        // given
        given(liveRepository.findById(1L)).willReturn(Optional.of(testLive));

        // when & then
        assertThatThrownBy(() -> liveService.deleteLive(otherSeller.getId(), 1L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("Live 판매자가 일치하지 않습니다");

        then(liveRepository).should(never()).delete(any(Live.class));
    }
}