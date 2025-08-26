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

import FreshBid.back.dto.product.CategoryCreateRequestDto;
import FreshBid.back.dto.product.CategoryUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("카테고리 컨트롤러 테스트")
class CategoryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testAdmin;
    private User testSeller;
    private ProductCategory parentCategory;
    private ProductCategory childCategory;
    private CategoryCreateRequestDto categoryCreateRequestDto;
    private CategoryUpdateRequestDto categoryUpdateRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        testAdmin = createTestAdmin();
        testSeller = createTestSeller();
        parentCategory = createParentCategory();
        childCategory = createChildCategory();

        categoryCreateRequestDto = new CategoryCreateRequestDto();
        categoryCreateRequestDto.setName("새로운 카테고리");
        categoryCreateRequestDto.setSuperId(null);

        categoryUpdateRequestDto = new CategoryUpdateRequestDto();
        categoryUpdateRequestDto.setName("수정된 카테고리");
        categoryUpdateRequestDto.setSuperId(parentCategory.getId());
    }

    private User createTestAdmin() {
        User user = new User();
        user.setUsername("testadmin");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_ADMIN);
        user.setNickname("테스트관리자");
        user.setEmail("admin@test.com");
        user.setPhoneNumber("010-1234-5678");
        user.setIntroduction("관리자입니다");
        user.setAccountNumber("123-456-789");
        user.setAddress("서울시 중구");
        return userRepository.save(user);
    }

    private User createTestSeller() {
        User user = new User();
        user.setUsername("testseller");
        user.setPassword("{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.");
        user.setRole(User.Role.ROLE_SELLER);
        user.setNickname("테스트판매자");
        user.setEmail("seller@test.com");
        user.setPhoneNumber("010-9876-5432");
        user.setIntroduction("신선한 농산물을 판매합니다");
        user.setAccountNumber("987-654-321");
        user.setAddress("경기도 수원시");
        return userRepository.save(user);
    }

    private ProductCategory createParentCategory() {
        ProductCategory category = ProductCategory.builder()
            .name("농산물")
            .build();
        return categoryRepository.save(category);
    }

    private ProductCategory createChildCategory() {
        ProductCategory category = ProductCategory.builder()
            .name("과일류")
            .superCategory(parentCategory)
            .build();
        return categoryRepository.save(category);
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void getAllCategories_Success() throws Exception {

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(get("/categories")
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 전체 조회 성공"))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("카테고리 등록 성공 - 상위 카테고리 없음")
    void createCategory_Success_NoParent() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryCreateRequestDto);

        mockMvc.perform(post("/categories")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 등록 성공"));

        assertThat(categoryRepository.findByName("새로운 카테고리")).isPresent();
    }

    @Test
    @DisplayName("카테고리 등록 성공 - 상위 카테고리 있음")
    void createCategory_Success_WithParent() throws Exception {
        categoryCreateRequestDto.setName("하위 카테고리");
        categoryCreateRequestDto.setSuperId(parentCategory.getId());

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryCreateRequestDto);

        mockMvc.perform(post("/categories")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 등록 성공"));

        ProductCategory created = categoryRepository.findByName("하위 카테고리").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getSuperCategory()).isEqualTo(parentCategory);
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 관리자 권한 없음")
    void createCategory_Forbidden() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryCreateRequestDto);

        mockMvc.perform(post("/categories")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated());    // TODO : 관리자 권한으로 수정 예정 (이하 동일)
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 존재하지 않는 상위 카테고리")
    void createCategory_ParentNotFound() throws Exception {
        categoryCreateRequestDto.setSuperId(999);

        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryCreateRequestDto);

        mockMvc.perform(post("/categories")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("상위 카테고리가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() throws Exception {
        Integer categoryId = childCategory.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryUpdateRequestDto);

        mockMvc.perform(put("/categories/{id}", categoryId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 수정 성공"));

        ProductCategory updated = categoryRepository.findById(categoryId).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("수정된 카테고리");
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_NotFound() throws Exception {
        Integer nonExistentId = 999;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryUpdateRequestDto);

        mockMvc.perform(put("/categories/{id}", nonExistentId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("해당 카테고리가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 관리자 권한 없음")
    void updateCategory_Forbidden() throws Exception {
        Integer categoryId = childCategory.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String requestBody = objectMapper.writeValueAsString(categoryUpdateRequestDto);

        mockMvc.perform(put("/categories/{id}", categoryId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk());        // TODO : 관리자 권한 전용으로 수정 예정
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() throws Exception {
        Integer categoryId = childCategory.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/categories/{id}", categoryId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));

        ProductCategory deleted = categoryRepository.findById(categoryId).orElse(null);
        assertThat(deleted).isNotNull();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_NotFound() throws Exception {
        Integer nonExistentId = 999;
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/categories/{id}", nonExistentId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("해당 카테고리가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 관리자 권한 없음")
    void deleteCategory_Forbidden() throws Exception {
        Integer categoryId = childCategory.getId();
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testSeller);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        mockMvc.perform(delete("/categories/{id}", categoryId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk());        // TODO : 관리자 권한 전용으로 수정 예정

        ProductCategory category = categoryRepository.findById(categoryId).orElse(null);
        assertThat(category).isNotNull();
        assertThat(category.isDeleted()).isTrue();        // TODO : 관리자 권한 전용으로 수정 예정
    }

    @Test
    @DisplayName("이름으로 카테고리 검색 성공")
    void getCategoryByName_Success() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String categoryName = parentCategory.getName();

        mockMvc.perform(get("/categories/search")
                .param("name", categoryName)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("카테고리 이름 검색 성공"))
            .andExpect(jsonPath("$.data.name").value(categoryName));
    }

    @Test
    @DisplayName("이름으로 카테고리 검색 실패 - 존재하지 않는 카테고리명")
    void getCategoryByName_NotFound() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        String nonExistentName = "존재하지않는카테고리";

        mockMvc.perform(get("/categories/search")
                .param("name", nonExistentName)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("해당 이름의 카테고리가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("상위 카테고리로 하위 카테고리 조회 성공")
    void getCategoriesBySuperId_Success() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        Integer superId = parentCategory.getId();

        mockMvc.perform(get("/categories/super/{superId}", superId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("하위 카테고리 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].name").value("과일류"));
    }

    @Test
    @DisplayName("상위 카테고리로 하위 카테고리 조회 - 하위 카테고리 없음")
    void getCategoriesBySuperId_Empty() throws Exception {
        FreshBidUserDetails userDetails = new FreshBidUserDetails(testAdmin);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        Integer superId = childCategory.getId();

        mockMvc.perform(get("/categories/super/{superId}", superId)
                .with(authentication(auth)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("하위 카테고리 목록 조회 성공"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }
}