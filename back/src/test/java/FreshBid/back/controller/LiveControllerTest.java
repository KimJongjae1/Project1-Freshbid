package FreshBid.back.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.repository.LiveRepository;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.service.impl.LiveServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("라이브 컨트롤러 테스트")
class LiveControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LiveRepository liveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private LiveServiceImpl liveService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private String liveTitle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AuctionRequestDto> auctions;
    private LiveCreateRequestDto liveCreateRequestDto;
    private LiveUpdateRequestDto liveUpdateRequestDto;
    private User testSeller;
    private User otherSeller;
    private ProductCategory testCategory;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // 테스트 데이터 생성
        testSeller = createTestUser();
        otherSeller = createOtherUser();
        testCategory = createTestCategory();
        testProducts = createTestProducts();

        liveTitle = "신선한 사과 경매";
        startDate = LocalDateTime.of(2025, 7, 25, 14, 0);
        endDate = LocalDateTime.of(2025, 7, 25, 16, 0);

        auctions = Arrays.asList(
            createAuctionRequestDto(testProducts.get(0).getId(), 10000L, 10),
            createAuctionRequestDto(testProducts.get(1).getId(), 15000L, 5),
            createAuctionRequestDto(testProducts.get(2).getId(), 8000L, 20)
        );

        liveCreateRequestDto = new LiveCreateRequestDto();
        liveCreateRequestDto.setTitle(liveTitle);
        liveCreateRequestDto.setStartDate(startDate);
        liveCreateRequestDto.setEndDate(endDate);
        liveCreateRequestDto.setAuctions(auctions);

        liveUpdateRequestDto = new LiveUpdateRequestDto();
        liveUpdateRequestDto.setTitle("수정된 사과 경매");
        liveUpdateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 26, 14, 0));
        liveUpdateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 26, 16, 0));
    }

    private AuctionRequestDto createAuctionRequestDto(long productId, long startPrice, int amount) {
        AuctionRequestDto dto = new AuctionRequestDto();
        dto.setProductId(productId);
        dto.setStartPrice(startPrice);
        dto.setAmount(amount);
        return dto;
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testseller");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_SELLER);
        user.setNickname("테스트판매자");
        user.setEmail("seller@test.com");
        user.setPhoneNumber("010-1234-5678");
        user.setIntroduction("신선한 농산물을 판매합니다");
        user.setAccountNumber("123-456-789");
        user.setAddress("경기도 수원시");
        return userRepository.save(user);
    }

    private User createOtherUser() {
        User user = new User();
        user.setUsername("testseller2");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_SELLER);
        user.setNickname("테스트판매자2");
        user.setEmail("seller2@test.com");
        user.setPhoneNumber("010-1234-5678");
        user.setIntroduction("신선한 농산물을 판매합니다2");
        user.setAccountNumber("123-456-789");
        user.setAddress("경기도 수원시");
        return userRepository.save(user);
    }

    private ProductCategory createTestCategory() {
        ProductCategory category = ProductCategory.builder()
            .name("과일류")
            .build();
        return productCategoryRepository.save(category);
    }

    private List<Product> createTestProducts() {
        Product product1 = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("홍로 사과")
            .origin("경북 예천")
            .weight(BigDecimal.valueOf(5.0))
            .description("당도 높은 신선한 홍로 사과입니다.")
            .build();

        Product product2 = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("방울토마토")
            .origin("전남 완도")
            .weight(BigDecimal.valueOf(2.5))
            .description("달콤하고 신선한 방울토마토입니다.")
            .build();

        Product product3 = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("김장배추")
            .origin("강원 평창")
            .weight(BigDecimal.valueOf(10.0))
            .description("김장용으로 최적인 신선한 배추입니다.")
            .build();

        return Arrays.asList(
            productRepository.save(product1),
            productRepository.save(product2),
            productRepository.save(product3)
        );
    }

    @Test
    @DisplayName("Live 등록 성공 - 라이브와 다중 경매 등록")
    void registerLive_Success() throws Exception {
        // given
        String requestBody = objectMapper.writeValueAsString(liveCreateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/live")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Live 등록에 성공했습니다."))
            .andExpect(jsonPath("$.data.title").value(liveTitle))
            .andExpect(jsonPath("$.data.startDate").exists())
            .andExpect(jsonPath("$.data.endDate").exists());

        List<Live> lives = liveRepository.findAll();
        assertThat(lives).hasSize(1);
        assertThat(lives.get(0).getTitle()).isEqualTo(liveTitle);
    }

    @Test
    @DisplayName("Live 등록 실패 - 일치하지 않는 판매자")
    void registerLive_UnauthorizedUser() throws Exception {
        // given
        String requestBody = objectMapper.writeValueAsString(liveCreateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/live").with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isForbidden());

        List<Live> lives = liveRepository.findAll();
        assertThat(lives).isEmpty();
    }

    @Test
    @DisplayName("Live 등록 실패 - 잘못된 시간 설정")
    void registerLive_InvalidTimeRange() throws Exception {
        // given
        LiveCreateRequestDto invalidTimeRequest = new LiveCreateRequestDto();
        invalidTimeRequest.setTitle(liveTitle);
        invalidTimeRequest.setStartDate(endDate); // 시작 시간이 종료 시간보다 늦음
        invalidTimeRequest.setEndDate(startDate);
        invalidTimeRequest.setAuctions(auctions);

        String requestBody = objectMapper.writeValueAsString(invalidTimeRequest);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/live")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());

        List<Live> lives = liveRepository.findAll();
        assertThat(lives).isEmpty();
    }

    @Test
    @DisplayName("Live 목록 조회 성공")
    void getAllLives_Success() throws Exception {
        // given
        LiveResponseDto savedResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/live")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "startDate")
                .param("sortDirection", "ASC")
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Live 목록 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").exists());
    }

    @Test
    @DisplayName("Live 단건 조회 성공")
    void getLive_Success() throws Exception {
        // given
        LiveResponseDto savedResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        Long liveId = savedResponse.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/live/{liveId}", liveId).with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Live 정보 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.id").value(liveId))
            .andExpect(jsonPath("$.data.title").value(liveTitle));
    }

    @Test
    @DisplayName("Live 단건 조회 실패 - 존재하지 않는 Live")
    void getLive_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/live/{liveId}", nonExistentId).with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Live 수정 성공")
    void updateLive_Success() throws Exception {
        // given
        LiveResponseDto savedResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        Long liveId = savedResponse.getId();

        String requestBody = objectMapper.writeValueAsString(liveUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/live/{liveId}", liveId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Live 수정에 성공했습니다."));
    }

    @Test
    @DisplayName("Live 수정 실패 - 존재하지 않는 Live")
    void updateLive_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        String requestBody = objectMapper.writeValueAsString(liveUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/live/{liveId}", nonExistentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Live 삭제 성공")
    void deleteLive_Success() throws Exception {
        // given
        LiveResponseDto savedResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        Long liveId = savedResponse.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/live/{liveId}", liveId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Live 삭제에 성공했습니다."));

        List<Live> lives = liveRepository.findAll();
        assertThat(lives).isEmpty();
    }

    @Test
    @DisplayName("Live 삭제 실패 - 존재하지 않는 Live")
    void deleteLive_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/live/{liveId}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Live 삭제 실패 - 일치하지 않는 판매자")
    void deleteLive_UnauthorizedUser() throws Exception {
        // given
        LiveResponseDto savedResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        Long liveId = savedResponse.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/live/{liveId}", liveId).with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden());

        List<Live> lives = liveRepository.findAll();
        assertThat(lives).hasSize(1); // 삭제되지 않음
    }
}