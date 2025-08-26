package FreshBid.back.repository;

import FreshBid.back.entity.ProductCategory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static FreshBid.back.entity.QProductCategory.productCategory;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public List<ProductCategory> findByOptionalSuperId(Integer superId) {
        return queryFactory
                .selectFrom(productCategory)
                .where(
                        superId != null
                                ? productCategory.superCategory.id.eq(superId)
                                : productCategory.superCategory.isNull()
                )
                .fetch();
    }
}
