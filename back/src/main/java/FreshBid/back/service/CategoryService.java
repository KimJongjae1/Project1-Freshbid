package FreshBid.back.service;

import FreshBid.back.dto.product.*;
import java.util.List;

public interface CategoryService {

    // 카테고리 등록 (관리자만 가능)
    void createCategory(CategoryCreateRequestDto dto);

    // 카테고리 수정 (관리자만 가능)
    void updateCategory(Integer categoryId, CategoryUpdateRequestDto dto);

    // 카테고리 삭제 (관리자만 가능)
    void deleteCategory(Integer categoryId);

    // 전체 카테고리 조회
    List<CategoryResponseDto> getAllCategories();

    // 🔍 이름으로 검색
    CategoryResponseDto getCategoryByName(String name);

    // 📂 상위 카테고리 ID로 하위 카테고리 조회
    List<CategoryResponseDto> getCategoriesBySuperId(Integer superId);
}
