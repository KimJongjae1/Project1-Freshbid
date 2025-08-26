package FreshBid.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import FreshBid.back.dto.product.CategoryCreateRequestDto;
import FreshBid.back.dto.product.CategoryResponseDto;
import FreshBid.back.dto.product.CategoryUpdateRequestDto;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.service.impl.CategoryServiceImpl;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("카테고리 서비스 테스트")
class CategoryServiceTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private ProductCategory parentCategory;
    private ProductCategory childCategory;
    private ProductCategory deletedCategory;
    private CategoryCreateRequestDto categoryCreateRequestDto;
    private CategoryUpdateRequestDto categoryUpdateRequestDto;

    @BeforeEach
    void setUp() {
        parentCategory = ProductCategory.builder()
            .name("농산물")
            .build();
        parentCategory.setId(1);

        childCategory = ProductCategory.builder()
            .name("과일류")
            .superCategory(parentCategory)
            .build();
        childCategory.setId(2);

        deletedCategory = ProductCategory.builder()
            .name("삭제된카테고리")
            .build();
        deletedCategory.setId(3);
        deletedCategory.setDeleted(true);

        categoryCreateRequestDto = new CategoryCreateRequestDto();
        categoryCreateRequestDto.setName("새로운 카테고리");
        categoryCreateRequestDto.setSuperId(null);

        categoryUpdateRequestDto = new CategoryUpdateRequestDto();
        categoryUpdateRequestDto.setName("수정된 카테고리");
        categoryUpdateRequestDto.setSuperId(parentCategory.getId());
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 상위 카테고리 없음")
    void createCategory_Success_NoParent() {
        given(categoryRepository.save(any(ProductCategory.class))).willReturn(parentCategory);

        categoryService.createCategory(categoryCreateRequestDto);

        then(categoryRepository).should().save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 상위 카테고리 있음")
    void createCategory_Success_WithParent() {
        categoryCreateRequestDto.setSuperId(parentCategory.getId());
        
        given(categoryRepository.findById(parentCategory.getId())).willReturn(Optional.of(parentCategory));
        given(categoryRepository.save(any(ProductCategory.class))).willReturn(childCategory);

        categoryService.createCategory(categoryCreateRequestDto);

        then(categoryRepository).should().findById(parentCategory.getId());
        then(categoryRepository).should().save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 존재하지 않는 상위 카테고리")
    void createCategory_ParentNotFound() {
        categoryCreateRequestDto.setSuperId(999);
        
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(categoryCreateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("상위 카테고리가 존재하지 않습니다.");

        then(categoryRepository).should().findById(999);
        then(categoryRepository).should(never()).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {
        given(categoryRepository.findById(childCategory.getId())).willReturn(Optional.of(childCategory));
        given(categoryRepository.findById(parentCategory.getId())).willReturn(Optional.of(parentCategory));
        given(categoryRepository.save(any(ProductCategory.class))).willReturn(childCategory);

        categoryService.updateCategory(childCategory.getId(), categoryUpdateRequestDto);

        then(categoryRepository).should().findById(childCategory.getId());
        then(categoryRepository).should().findById(parentCategory.getId());
        then(categoryRepository).should().save(childCategory);
        assertThat(childCategory.getName()).isEqualTo("수정된 카테고리");
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_NotFound() {
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999, categoryUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("해당 카테고리가 존재하지 않습니다.");

        then(categoryRepository).should().findById(999);
        then(categoryRepository).should(never()).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 상위 카테고리")
    void updateCategory_ParentNotFound() {
        categoryUpdateRequestDto.setSuperId(999);
        
        given(categoryRepository.findById(childCategory.getId())).willReturn(Optional.of(childCategory));
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(childCategory.getId(), categoryUpdateRequestDto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("상위 카테고리가 존재하지 않습니다.");

        then(categoryRepository).should().findById(childCategory.getId());
        then(categoryRepository).should().findById(999);
        then(categoryRepository).should(never()).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        given(categoryRepository.findById(childCategory.getId())).willReturn(Optional.of(childCategory));
        given(categoryRepository.save(any(ProductCategory.class))).willReturn(childCategory);

        categoryService.deleteCategory(childCategory.getId());

        then(categoryRepository).should().findById(childCategory.getId());
        then(categoryRepository).should().save(childCategory);
        assertThat(childCategory.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_NotFound() {
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(999))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("해당 카테고리가 존재하지 않습니다.");

        then(categoryRepository).should().findById(999);
        then(categoryRepository).should(never()).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void getAllCategories_Success() {
        List<ProductCategory> categories = Arrays.asList(parentCategory, childCategory, deletedCategory);
        given(categoryRepository.findAll()).willReturn(categories);

        List<CategoryResponseDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2); // 삭제되지 않은 카테고리만
        assertThat(result.get(0).getName()).isEqualTo("농산물");
        assertThat(result.get(1).getName()).isEqualTo("과일류");
        then(categoryRepository).should().findAll();
    }

    @Test
    @DisplayName("이름으로 카테고리 검색 성공")
    void getCategoryByName_Success() {
        given(categoryRepository.findByName("농산물")).willReturn(Optional.of(parentCategory));

        CategoryResponseDto result = categoryService.getCategoryByName("농산물");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("농산물");
        then(categoryRepository).should().findByName("농산물");
    }

    @Test
    @DisplayName("이름으로 카테고리 검색 실패 - 존재하지 않는 카테고리")
    void getCategoryByName_NotFound() {
        given(categoryRepository.findByName("존재하지않는카테고리")).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryByName("존재하지않는카테고리"))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("해당 이름의 카테고리가 존재하지 않습니다.");

        then(categoryRepository).should().findByName("존재하지않는카테고리");
    }

    @Test
    @DisplayName("상위 카테고리로 하위 카테고리 조회 성공")
    void getCategoriesBySuperId_Success() {
        List<ProductCategory> childCategories = Arrays.asList(childCategory);
        given(categoryRepository.findBySuperCategoryId(parentCategory.getId())).willReturn(childCategories);

        List<CategoryResponseDto> result = categoryService.getCategoriesBySuperId(parentCategory.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("과일류");
        then(categoryRepository).should().findBySuperCategoryId(parentCategory.getId());
    }

    @Test
    @DisplayName("상위 카테고리로 하위 카테고리 조회 - 하위 카테고리 없음")
    void getCategoriesBySuperId_Empty() {
        given(categoryRepository.findBySuperCategoryId(childCategory.getId())).willReturn(Arrays.asList());

        List<CategoryResponseDto> result = categoryService.getCategoriesBySuperId(childCategory.getId());

        assertThat(result).isEmpty();
        then(categoryRepository).should().findBySuperCategoryId(childCategory.getId());
    }

    @Test
    @DisplayName("상위 카테고리로 하위 카테고리 조회 - 삭제된 카테고리 제외")
    void getCategoriesBySuperId_ExcludeDeleted() {
        ProductCategory anotherChild = ProductCategory.builder()
            .name("삭제된하위카테고리")
            .superCategory(parentCategory)
            .build();
        anotherChild.setDeleted(true);

        List<ProductCategory> childCategories = Arrays.asList(childCategory, anotherChild);
        given(categoryRepository.findBySuperCategoryId(parentCategory.getId())).willReturn(childCategories);

        List<CategoryResponseDto> result = categoryService.getCategoriesBySuperId(parentCategory.getId());

        assertThat(result).hasSize(1); // 삭제되지 않은 카테고리만
        assertThat(result.get(0).getName()).isEqualTo("과일류");
        then(categoryRepository).should().findBySuperCategoryId(parentCategory.getId());
    }
}