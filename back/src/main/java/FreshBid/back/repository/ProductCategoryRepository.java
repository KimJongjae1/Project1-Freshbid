package FreshBid.back.repository;

import FreshBid.back.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    // 🔍 이름으로 검색
    Optional<ProductCategory> findByName(String name);

    // 📂 상위 카테고리 ID로 하위 카테고리 리스트 조회
    List<ProductCategory> findBySuperCategoryId(Integer superId);
    
    // 🏠 상위 카테고리 조회 (superCategory가 null인 것들)
    List<ProductCategory> findBySuperCategoryIsNull();
    
    // 📂 특정 상위 카테고리의 하위 카테고리 조회
    List<ProductCategory> findBySuperCategory(ProductCategory superCategory);
}
