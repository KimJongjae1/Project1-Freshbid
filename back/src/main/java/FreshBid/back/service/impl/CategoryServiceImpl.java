package FreshBid.back.service.impl;

import FreshBid.back.dto.product.*;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Override
    public void createCategory(CategoryCreateRequestDto dto) {
        log.info("카테고리 생성 요청 - 이름: {}, 상위 ID: {}", dto.getName(), dto.getSuperId());

        ProductCategory superCategory = getSuperCategoryOrNull(dto.getSuperId());
        ProductCategory category = ProductCategory.of(dto.getName(), superCategory);
        categoryRepository.save(category);

        log.info("카테고리 생성 완료 - ID: {}", category.getId());
    }

    @Override
    public void updateCategory(Integer categoryId, CategoryUpdateRequestDto dto) {
        log.info("카테고리 수정 요청 - ID: {}, 변경 내용: {}", categoryId, dto);

        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("카테고리 수정 실패 - 존재하지 않는 ID: {}", categoryId);
                    return new NotFoundException("해당 카테고리가 존재하지 않습니다.");
                });

        if (dto.getName() != null) {
            category.setName(dto.getName());
        }

        if (dto.getSuperId() != null) {
            category.setSuperCategory(getSuperCategoryOrNull(dto.getSuperId()));
        }

        categoryRepository.save(category);
        log.info("카테고리 수정 완료 - ID: {}", categoryId);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        log.info("카테고리 삭제 요청 - ID: {}", categoryId);

        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("카테고리 삭제 실패 - 존재하지 않는 ID: {}", categoryId);
                    return new NotFoundException("해당 카테고리가 존재하지 않습니다.");
                });

        category.setDeleted(true);
        categoryRepository.save(category);

        log.info("카테고리 삭제 완료 - ID: {}", categoryId);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        log.info("전체 카테고리 조회 요청");

        List<CategoryResponseDto> result = categoryRepository.findAll().stream()
                .filter(category -> !category.isDeleted())
                .map(CategoryResponseDto::fromEntity)
                .toList();

        log.info("카테고리 조회 완료 - {}건", result.size());
        return result;
    }

    @Override
    public CategoryResponseDto getCategoryByName(String name) {
        log.info("카테고리 이름 조회 요청 - 이름: {}", name);

        ProductCategory category = categoryRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("카테고리 이름 조회 실패 - 존재하지 않음: {}", name);
                    return new NotFoundException("해당 이름의 카테고리가 존재하지 않습니다.");
                });

        log.info("카테고리 이름 조회 완료 - ID: {}", category.getId());
        return CategoryResponseDto.fromEntity(category);
    }

    @Override
    public List<CategoryResponseDto> getCategoriesBySuperId(Integer superId) {
        log.info("상위 카테고리 하위 목록 조회 요청 - 상위 ID: {}", superId);

        List<CategoryResponseDto> result = categoryRepository.findBySuperCategoryId(superId).stream()
                .filter(c -> !c.isDeleted())
                .map(CategoryResponseDto::fromEntity)
                .toList();

        log.info("하위 카테고리 조회 완료 - {}건", result.size());
        return result;
    }

    private ProductCategory getSuperCategoryOrNull(Integer superId) {
        if (superId == null) return null;

        return categoryRepository.findById(superId)
                .orElseThrow(() -> {
                    log.warn("상위 카테고리 조회 실패 - 존재하지 않음: {}", superId);
                    return new NotFoundException("상위 카테고리가 존재하지 않습니다.");
                });
    }
}
