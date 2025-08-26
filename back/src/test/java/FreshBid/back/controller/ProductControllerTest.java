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

import FreshBid.back.dto.product.ProductCreateRequestDto;
import FreshBid.back.dto.product.ProductUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testSeller;
    private User otherSeller;
    private ProductCategory testCategory;
    private Product testProduct;
    private ProductCreateRequestDto productCreateRequestDto;
    private ProductUpdateRequestDto productUpdateRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        testSeller = createTestSeller();
        otherSeller = createOtherSeller();
        testCategory = createTestCategory();
        testProduct = createTestProduct();

        productCreateRequestDto = new ProductCreateRequestDto();
        productCreateRequestDto.setName("신선한 토마토");
        productCreateRequestDto.setOrigin("전남 완도");
        productCreateRequestDto.setWeight(BigDecimal.valueOf(2.5));
        productCreateRequestDto.setDescription("당도 높은 신선한 토마토입니다.");
        productCreateRequestDto.setCategoryId(testCategory.getId());
        productCreateRequestDto.setGrade("특");

        productUpdateRequestDto = new ProductUpdateRequestDto();
        productUpdateRequestDto.setName("수정된 사과");
        productUpdateRequestDto.setOrigin("경북 안동");
        productUpdateRequestDto.setWeight(BigDecimal.valueOf(3.0));
        productUpdateRequestDto.setDescription("수정된 상품 설명");
        productUpdateRequestDto.setGrade("상");
    }

    private User createTestSeller() {
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

    private User createOtherSeller() {
        User user = new User();
        user.setUsername("otherseller");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_SELLER);
        user.setNickname("다른판매자");
        user.setEmail("other@test.com");
        user.setPhoneNumber("010-9876-5432");
        user.setIntroduction("다른 농산물을 판매합니다");
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

    private Product createTestProduct() {
        Product product = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("홍로 사과")
            .origin("경북 예천")
            .weight(BigDecimal.valueOf(5.0))
            .description("당도 높은 신선한 홍로 사과입니다.")
            .grade(Product.Grade.특)
            .build();
        return productRepository.save(product);
    }

    @Test
    @DisplayName("상품 등록 성공")
    void createProduct_Success() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productCreateRequestDto);

        mockMvc.perform(post("/auction/product")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상품 등록에 성공했습니다."))
            .andExpect(jsonPath("$.data.name").value("신선한 토마토"))
            .andExpect(jsonPath("$.data.origin").value("전남 완도"));
    }

    @Test
    @DisplayName("상품 등록 실패 - 상품 이름 누락")
    void createProduct_MissingName() throws Exception {
        productCreateRequestDto.setName(null);
        
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productCreateRequestDto);

        mockMvc.perform(post("/auction/product")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("상품 이름은 필수입니다."));
    }

    @Test
    @DisplayName("상품 등록 실패 - 존재하지 않는 카테고리")
    void createProduct_CategoryNotFound() throws Exception {
        productCreateRequestDto.setCategoryId(999);
        
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productCreateRequestDto);

        mockMvc.perform(post("/auction/product")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다: 999"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    void searchProducts_Success() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(get("/auction/product")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "name")
                .param("sortDirection", "ASC")
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상품 목록 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("상품 단건 조회 성공")
    void getProduct_Success() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        Long productId = testProduct.getId();

        mockMvc.perform(get("/auction/product/{productId}", productId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상품 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data.id").value(productId))
            .andExpect(jsonPath("$.data.name").value("홍로 사과"));
    }

    @Test
    @DisplayName("상품 단건 조회 실패 - 존재하지 않는 상품")
    void getProduct_NotFound() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        Long nonExistentId = 999999L;

        mockMvc.perform(get("/auction/product/{productId}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_Success() throws Exception {
        Long productId = testProduct.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productUpdateRequestDto);

        mockMvc.perform(put("/auction/product/{productId}", productId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상품 수정에 성공했습니다."));

        Product updatedProduct = productRepository.findById(productId).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("수정된 사과");
        assertThat(updatedProduct.getOrigin()).isEqualTo("경북 안동");
    }

    @Test
    @DisplayName("상품 수정 실패 - 권한 없는 사용자")
    void updateProduct_Forbidden() throws Exception {
        Long productId = testProduct.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productUpdateRequestDto);

        mockMvc.perform(put("/auction/product/{productId}", productId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("상품 수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
    void updateProduct_NotFound() throws Exception {
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(productUpdateRequestDto);

        mockMvc.perform(put("/auction/product/{productId}", nonExistentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다: " + nonExistentId));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_Success() throws Exception {
        Long productId = testProduct.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/auction/product/{productId}", productId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상품 삭제에 성공했습니다."));

        Product deletedProduct = productRepository.findById(productId).orElse(null);
        assertThat(deletedProduct).isNotNull();
        assertThat(deletedProduct.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 권한 없는 사용자")
    void deleteProduct_Forbidden() throws Exception {
        Long productId = testProduct.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(otherSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/auction/product/{productId}", productId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("상품 삭제 권한이 없습니다."));

        Product product = productRepository.findById(productId).orElse(null);
        assertThat(product).isNotNull();
        assertThat(product.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
    void deleteProduct_NotFound() throws Exception {
        Long nonExistentId = 999999L;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/auction/product/{productId}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다: " + nonExistentId));
    }
}