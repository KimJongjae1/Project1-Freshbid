package FreshBid.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import FreshBid.back.dto.product.ProductCreateRequestDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.product.ProductSearchRequestDto;
import FreshBid.back.dto.product.ProductUpdateRequestDto;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.ProductRepositorySupport;
import FreshBid.back.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
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
@DisplayName("상품 서비스 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductRepositorySupport productRepositorySupport;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testSeller;
    private User otherSeller;
    private ProductCategory testCategory;
    private Product testProduct;
    private ProductCreateRequestDto productCreateRequestDto;
    private ProductUpdateRequestDto productUpdateRequestDto;
    private ProductSearchRequestDto productSearchRequestDto;

    @BeforeEach
    void setUp() {
        testSeller = createTestUser(1L, "testseller");
        otherSeller = createTestUser(2L, "otherseller");

        testCategory = ProductCategory.builder()
            .name("과일류")
            .build();
        testCategory.setId(1);

        testProduct = Product.builder()
            .user(testSeller)
            .category(testCategory)
            .name("홍로 사과")
            .origin("경북 예천")
            .weight(BigDecimal.valueOf(5.0))
            .description("당도 높은 신선한 홍로 사과입니다.")
            .grade(Product.Grade.특)
            .build();
        testProduct.setId(1L);

        productCreateRequestDto = new ProductCreateRequestDto();
        productCreateRequestDto.setName("신선한 토마토");
        productCreateRequestDto.setOrigin("전남 완도");
        productCreateRequestDto.setWeight(BigDecimal.valueOf(2.5));
        productCreateRequestDto.setDescription("당도 높은 신선한 토마토입니다.");
        productCreateRequestDto.setCategoryId(1);
        productCreateRequestDto.setGrade("PREMIUM");

        productUpdateRequestDto = new ProductUpdateRequestDto();
        productUpdateRequestDto.setName("수정된 사과");
        productUpdateRequestDto.setOrigin("경북 안동");
        productUpdateRequestDto.setWeight(BigDecimal.valueOf(3.0));
        productUpdateRequestDto.setDescription("수정된 상품 설명");
        productUpdateRequestDto.setGrade("상");

        productSearchRequestDto = new ProductSearchRequestDto();
        productSearchRequestDto.setPage(0);
        productSearchRequestDto.setSize(20);
        productSearchRequestDto.setSortBy("name");
        productSearchRequestDto.setSortDirection("ASC");
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
    @DisplayName("상품 등록 성공")
    void createProduct_Success() {
        given(categoryRepository.findById(1)).willReturn(Optional.of(testCategory));
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L);
            return savedProduct;
        });

        ProductResponseDto result = productService.createProduct(productCreateRequestDto, testSeller);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("신선한 토마토");
        assertThat(result.getOrigin()).isEqualTo("전남 완도");
        assertThat(result.getWeight()).isEqualTo("2.5");
        then(categoryRepository).should().findById(1);
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 상품 이름 누락")
    void createProduct_MissingName() {
        productCreateRequestDto.setName(null);

        assertThatThrownBy(() -> productService.createProduct(productCreateRequestDto, testSeller))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("상품 이름은 필수입니다.");

        then(categoryRepository).should(never()).findById(any());
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 무게 누락")
    void createProduct_MissingWeight() {
        productCreateRequestDto.setWeight(null);

        assertThatThrownBy(() -> productService.createProduct(productCreateRequestDto, testSeller))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("상품 무게는 필수입니다.");

        then(categoryRepository).should(never()).findById(any());
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 원산지 누락")
    void createProduct_MissingOrigin() {
        productCreateRequestDto.setOrigin(null);

        assertThatThrownBy(() -> productService.createProduct(productCreateRequestDto, testSeller))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("원산지는 필수입니다.");

        then(categoryRepository).should(never()).findById(any());
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 카테고리 ID 누락")
    void createProduct_MissingCategoryId() {
        productCreateRequestDto.setCategoryId(null);

        assertThatThrownBy(() -> productService.createProduct(productCreateRequestDto, testSeller))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리 ID는 필수입니다.");

        then(categoryRepository).should(never()).findById(any());
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 존재하지 않는 카테고리")
    void createProduct_CategoryNotFound() {
        given(categoryRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(productCreateRequestDto, testSeller))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 카테고리입니다: 1");

        then(categoryRepository).should().findById(1);
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() {
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        ProductResponseDto result = productService.getProduct(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("홍로 사과");
        then(productRepository).should().findById(1L);
    }

    @Test
    @DisplayName("상품 조회 실패 - 존재하지 않는 상품")
    void getProduct_NotFound() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 상품입니다: 999");

        then(productRepository).should().findById(999L);
    }

    @Test
    @DisplayName("상품 검색 성공")
    void searchProducts_Success() {
        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList);
        given(productRepositorySupport.searchProductsByCondition(any(), any())).willReturn(productPage);

        Page<ProductResponseDto> result = productService.searchProducts(productSearchRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("홍로 사과");
        then(productRepositorySupport).should().searchProductsByCondition(any(), any());
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_Success() {
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        productService.updateProduct(testSeller.getId(), 1L, productUpdateRequestDto);

        then(productRepository).should().findById(1L);
        then(productRepository).should().save(testProduct);
        assertThat(testProduct.getName()).isEqualTo("수정된 사과");
        assertThat(testProduct.getOrigin()).isEqualTo("경북 안동");
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
    void updateProduct_NotFound() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(testSeller.getId(), 999L, productUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 상품입니다: 999");

        then(productRepository).should().findById(999L);
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 실패 - 권한 없는 사용자")
    void updateProduct_Forbidden() {
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> productService.updateProduct(otherSeller.getId(), 1L, productUpdateRequestDto))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("상품 수정 권한이 없습니다.");

        then(productRepository).should().findById(1L);
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 성공 - 카테고리 변경")
    void updateProduct_Success_CategoryChange() {
        ProductCategory newCategory = ProductCategory.builder()
            .name("채소류")
            .build();
        newCategory.setId(2);

        productUpdateRequestDto.setCategoryId(2);
        
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(categoryRepository.findById(2)).willReturn(Optional.of(newCategory));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        productService.updateProduct(testSeller.getId(), 1L, productUpdateRequestDto);

        then(productRepository).should().findById(1L);
        then(categoryRepository).should().findById(2);
        then(productRepository).should().save(testProduct);
        assertThat(testProduct.getCategory()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 카테고리")
    void updateProduct_CategoryNotFound() {
        productUpdateRequestDto.setCategoryId(999);
        
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(testSeller.getId(), 1L, productUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 카테고리입니다: 999");

        then(productRepository).should().findById(1L);
        then(categoryRepository).should().findById(999);
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_Success() {
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        productService.deleteProduct(testSeller.getId(), 1L);

        then(productRepository).should().findById(1L);
        then(productRepository).should().save(testProduct);
        assertThat(testProduct.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
    void deleteProduct_NotFound() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(testSeller.getId(), 999L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 상품입니다: 999");

        then(productRepository).should().findById(999L);
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 삭제 실패 - 권한 없는 사용자")
    void deleteProduct_Forbidden() {
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> productService.deleteProduct(otherSeller.getId(), 1L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("상품 삭제 권한이 없습니다.");

        then(productRepository).should().findById(1L);
        then(productRepository).should(never()).save(any(Product.class));
        assertThat(testProduct.isDeleted()).isFalse();
    }
}