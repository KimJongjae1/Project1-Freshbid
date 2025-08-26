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
import static org.hamcrest.Matchers.containsString;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.dto.bid.BidRequestDto;
import FreshBid.back.dto.live.LiveCreateRequestDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.repository.AuctionRepository;
import FreshBid.back.repository.BidRedisRepositorySupport;
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
@DisplayName("경매 컨트롤러 테스트")
class AuctionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuctionRepository auctionRepository;

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
    private BidRedisRepositorySupport bidRedisRepositorySupport;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testSeller;
    private User otherSeller;
    private User testBuyer;
    private ProductCategory testCategory;
    private List<Product> testProducts;
    private Live testLive;
    private Auction testAuction;
    private AuctionUpdateRequestDto auctionUpdateRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // 테스트 데이터 생성
        testSeller = createTestUser();
        otherSeller = createOtherUser();
        testBuyer = createTestBuyer();
        testCategory = createTestCategory();
        testProducts = createTestProducts();
        testLive = createTestLive();
        testAuction = createTestAuction();

        // Redis 데이터 정리 (각 테스트 전에 해당 경매의 입찰 데이터 정리)
        bidRedisRepositorySupport.deleteByAuctionId(testAuction.getId());

        // 경매 수정 요청 DTO
        auctionUpdateRequestDto = new AuctionUpdateRequestDto();
        auctionUpdateRequestDto.setStartPrice(20000L);
        auctionUpdateRequestDto.setAmount(15);
        auctionUpdateRequestDto.setStatus(Auction.Status.IN_PROGRESS);
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

    private User createTestBuyer() {
        User user = new User();
        user.setUsername("testbuyer");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_CUSTOMER);
        user.setNickname("테스트구매자");
        user.setEmail("buyer@test.com");
        user.setPhoneNumber("010-9876-5432");
        user.setIntroduction("신선한 농산물을 구매합니다");
        user.setAccountNumber("987-654-321");
        user.setAddress("서울시 강남구");
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

        return Arrays.asList(
            productRepository.save(product1),
            productRepository.save(product2)
        );
    }

    private Live createTestLive() {
        AuctionRequestDto auctionRequestDto = new AuctionRequestDto();
        auctionRequestDto.setProductId(testProducts.get(0).getId());
        auctionRequestDto.setStartPrice(10000L);
        auctionRequestDto.setAmount(10);

        LiveCreateRequestDto liveCreateRequestDto = new LiveCreateRequestDto();
        liveCreateRequestDto.setTitle("신선한 사과 경매");
        liveCreateRequestDto.setStartDate(LocalDateTime.of(2025, 7, 25, 14, 0));
        liveCreateRequestDto.setEndDate(LocalDateTime.of(2025, 7, 25, 16, 0));
        liveCreateRequestDto.setAuctions(Arrays.asList(auctionRequestDto));

        LiveResponseDto liveResponse = liveService.createLiveWithAuctions(liveCreateRequestDto,
            testSeller);
        return liveRepository.findById(liveResponse.getId()).orElse(null);
    }

    private Auction createTestAuction() {
        return auctionRepository.findAll().get(0);
    }

    @Test
    @DisplayName("경매 목록 조회 성공")
    void getAllAuctions_Success() throws Exception {
        // given
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "startPrice")
                .param("sortDirection", "ASC")
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("경매 목록 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("경매 단건 조회 성공")
    void getAuction_Success() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/{auctionId}", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("경매 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.id").value(auctionId))
            .andExpect(jsonPath("$.data.startPrice").exists())
            .andExpect(jsonPath("$.data.amount").exists());
    }

    @Test
    @DisplayName("경매 단건 조회 실패 - 존재하지 않는 경매")
    void getAuction_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/{auctionId}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("경매 수정 성공")
    void updateAuction_Success() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        String requestBody = objectMapper.writeValueAsString(auctionUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/{auctionId}", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("경매 수정에 성공했습니다."));

        // 수정된 내용 검증
        Auction updatedAuction = auctionRepository.findById(auctionId).orElse(null);
        assertThat(updatedAuction).isNotNull();
        assertThat(updatedAuction.getStartPrice()).isEqualTo(20000L);
        assertThat(updatedAuction.getAmount()).isEqualTo(15);
    }

    @Test
    @DisplayName("경매 수정 실패 - 존재하지 않는 경매")
    void updateAuction_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        String requestBody = objectMapper.writeValueAsString(auctionUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/{auctionId}", nonExistentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("경매 수정 실패 - 권한 없는 사용자")
    void updateAuction_Forbidden() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        String requestBody = objectMapper.writeValueAsString(auctionUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/{auctionId}", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("경매 상품 판매자가 일치하지 않습니다"));
    }

    @Test
    @DisplayName("경매 수정 실패 - 잘못된 시작 가격")
    void updateAuction_InvalidStartPrice() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        auctionUpdateRequestDto.setStartPrice(-1000L); // 음수 가격
        String requestBody = objectMapper.writeValueAsString(auctionUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/{auctionId}", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("시작 가격은 0보다 커야 합니다"));
    }

    @Test
    @DisplayName("경매 수정 실패 - 잘못된 수량")
    void updateAuction_InvalidAmount() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        auctionUpdateRequestDto.setAmount(-5); // 음수 수량
        String requestBody = objectMapper.writeValueAsString(auctionUpdateRequestDto);
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(put("/auction/{auctionId}", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("수량은 0보다 커야 합니다"));
    }

    @Test
    @DisplayName("경매 삭제 성공")
    void deleteAuction_Success() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/{auctionId}", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("경매 삭제에 성공했습니다."));

        // 삭제 확인
        assertThat(auctionRepository.findById(auctionId)).isEmpty();
    }

    @Test
    @DisplayName("경매 삭제 실패 - 존재하지 않는 경매")
    void deleteAuction_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/{auctionId}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("경매 삭제 실패 - 권한 없는 사용자")
    void deleteAuction_Forbidden() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(delete("/auction/{auctionId}", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("경매 상품 판매자가 일치하지 않습니다"));

        // 삭제되지 않았는지 확인
        assertThat(auctionRepository.findById(auctionId)).isPresent();
    }

    // =================== 경매 시작 테스트 ===================
    
    @Test
    @DisplayName("경매 시작 성공")
    void startAuction_Success() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/start", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("경매 시작에 성공했습니다."));

        // 상태 변경 확인
        Auction updatedAuction = auctionRepository.findById(auctionId).orElse(null);
        assertThat(updatedAuction).isNotNull();
        assertThat(updatedAuction.getStatus()).isEqualTo(Auction.Status.IN_PROGRESS);
    }

    @Test
    @DisplayName("경매 시작 실패 - 존재하지 않는 경매")
    void startAuction_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/start", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("경매 시작 실패 - 권한 없는 사용자")
    void startAuction_Forbidden() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/start", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("경매 상품 판매자가 일치하지 않습니다"));
    }

    @Test
    @DisplayName("경매 시작 실패 - 잘못된 상태")
    void startAuction_WrongStatus() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        // 경매를 IN_PROGRESS 상태로 변경
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/start", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(containsString("현재 경매 상태에서 진행할 수 없는 작업입니다")));
    }

    // =================== 경매 종료 테스트 ===================

    @Test  
    @DisplayName("경매 종료 성공 - 입찰 없음")
    void endAuction_Success_NoBids() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        // 경매를 IN_PROGRESS 상태로 변경
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/end", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(containsString("경매 종료에 성공했습니다")))
            .andExpect(jsonPath("$.data").isEmpty());

        // 상태 변경 확인
        Auction updatedAuction = auctionRepository.findById(auctionId).orElse(null);
        assertThat(updatedAuction).isNotNull();
        assertThat(updatedAuction.getStatus()).isEqualTo(Auction.Status.ENDED);
    }

    @Test
    @DisplayName("경매 종료 실패 - 존재하지 않는 경매")
    void endAuction_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/end", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("경매 종료 실패 - 권한 없는 사용자")
    void endAuction_Forbidden() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/end", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("경매 상품 판매자가 일치하지 않습니다"));
    }

    @Test
    @DisplayName("경매 종료 실패 - 잘못된 상태")
    void endAuction_WrongStatus() throws Exception {
        // given - 경매가 SCHEDULED 상태
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/end", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(containsString("현재 경매 상태에서 진행할 수 없는 작업입니다")));
    }

    // =================== 입찰 제출 테스트 ===================

    @Test
    @DisplayName("입찰 제출 성공")
    void createBid_Success() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        // 경매를 IN_PROGRESS 상태로 변경
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        BidRequestDto bidRequest = new BidRequestDto();
        bidRequest.setBidPrice(15000L);
        String requestBody = objectMapper.writeValueAsString(bidRequest);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/bid", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("입찰이 성공적으로 제출되었습니다."));
    }

    @Test
    @DisplayName("입찰 제출 실패 - 존재하지 않는 경매")
    void createBid_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        BidRequestDto bidRequest = new BidRequestDto();
        bidRequest.setBidPrice(15000L);
        String requestBody = objectMapper.writeValueAsString(bidRequest);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/bid", nonExistentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다."));
    }

    @Test
    @DisplayName("입찰 제출 실패 - 경매 진행중이 아님")
    void createBid_ForbiddenStatus() throws Exception {
        // given - 경매가 SCHEDULED 상태
        Long auctionId = testAuction.getId();
        BidRequestDto bidRequest = new BidRequestDto();
        bidRequest.setBidPrice(15000L);
        String requestBody = objectMapper.writeValueAsString(bidRequest);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/bid", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("진행 중인 경매가 아닙니다."));
    }

    @Test
    @DisplayName("입찰 제출 성공 - 입찰가가 너무 낮음")
    void createBid_BidTooLow() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        BidRequestDto bidRequest = new BidRequestDto();
        bidRequest.setBidPrice(5000L); // 시작가(10000L)보다 낮음
        String requestBody = objectMapper.writeValueAsString(bidRequest);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/bid", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("입찰 제출 실패 - 잘못된 입찰가")
    void createBid_InvalidBidPrice() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        testAuction.setStatus(Auction.Status.IN_PROGRESS);
        auctionRepository.save(testAuction);

        BidRequestDto bidRequest = new BidRequestDto();
        bidRequest.setBidPrice(500L); // 최소 입찰가(1000L)보다 낮음
        String requestBody = objectMapper.writeValueAsString(bidRequest);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(post("/auction/{auctionId}/bid", auctionId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("입찰 가격은 최소 1,000원 이상이어야 합니다."));
    }

    // =================== 입찰 현황 조회 테스트 ===================

    @Test
    @DisplayName("입찰 현황 조회 성공 - 입찰 없음")
    void getAllBids_Success_NoBids() throws Exception {
        // given
        Long auctionId = testAuction.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/{auctionId}/bid", auctionId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("입찰 현황을 성공적으로 조회하였습니다."))
            .andExpect(jsonPath("$.data.auctionId").value(auctionId))
            .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
            .andExpect(jsonPath("$.data.currentHighestPrice").value(10000L)) // 시작가
            .andExpect(jsonPath("$.data.bidList").isArray())
            .andExpect(jsonPath("$.data.bidList").isEmpty())
            .andExpect(jsonPath("$.data.highestBid").isEmpty());
    }

    @Test
    @DisplayName("입찰 현황 조회 실패 - 존재하지 않는 경매")
    void getAllBids_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testBuyer);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/auction/{auctionId}/bid", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 경매입니다."));
    }
}