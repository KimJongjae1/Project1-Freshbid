package FreshBid.back.repository;

import static FreshBid.back.entity.QProduct.product;
import static FreshBid.back.entity.QUser.user;
import static FreshBid.back.entity.QProductCategory.productCategory;

import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.product.ProductSearchRequestDto;
import FreshBid.back.entity.Product;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public ProductRepositorySupport(JPAQueryFactory queryFactory) {
        super(Product.class);
        this.queryFactory = queryFactory;
    }

    // 삭제되지 않은 모든 상품 조회
    public List<Product> findAllNotDeleted() {
        return queryFactory
                .selectFrom(product)
                .join(product.user, user).fetchJoin()
                .join(product.category, productCategory).fetchJoin()
                .where(product.deleted.eq(false))
                .fetch();
    }

    // 특정 유저의 상품 조회
    public List<Product> findByUserId(Long userId) {
        return queryFactory
                .selectFrom(product)
                .join(product.user, user).fetchJoin()
                .join(product.category, productCategory).fetchJoin()
                .where(product.user.id.eq(userId)
                        .and(product.deleted.eq(false)))
                .fetch();
    }

    // 검색 + 정렬 + 페이징
    public Page<Product> searchProductsByCondition(ProductSearchRequestDto req, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (req.getName() != null && !req.getName().isBlank()) {
            builder.and(product.name.contains(req.getName()));
        }

        if (req.getCategoryId() != null) {
            builder.and(product.category.id.eq(req.getCategoryId()));
        }

        if (req.getGrade() != null) {
            builder.and(product.grade.eq(Product.Grade.valueOf(req.getGrade())));
        }

        if (req.getMinWeight() != null) {
            builder.and(product.weight.goe(req.getMinWeight()));
        }

        if (req.getMaxWeight() != null) {
            builder.and(product.weight.loe(req.getMaxWeight()));
        }

        builder.and(product.deleted.eq(false));

        Order direction = Order.valueOf(req.getSortDirection().toUpperCase());
        PathBuilder<Product> path = new PathBuilder<>(Product.class, "product");
        OrderSpecifier<Comparable> orderSpecifier = new OrderSpecifier<>(direction, path.getComparable(req.getSortBy(), Comparable.class));

        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.user, user).fetchJoin()
                .join(product.category, productCategory).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> queryFactory
                        .select(product.count())
                        .from(product)
                        .where(builder)
                        .fetchOne()
        );
    }

    public Page<ProductResponseDto> getProductsBySellerId(Long sellerId, Integer category, Pageable pageable) {
        List<Product> products = queryFactory
                .selectFrom(product)
                .join(product.category, productCategory).fetchJoin()
                .join(product.user, user).fetchJoin()
                .where(filterCategory(sellerId, category))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        List<ProductResponseDto> productsDto = products.stream()
                .map(ProductResponseDto::toDto).toList();

        Long count = queryFactory
                .select(product.countDistinct())
                .from(product)
                .where(filterCategory(sellerId, category))
                .fetchOne();

        return new PageImpl<>(productsDto, pageable, count);
    }

    private BooleanBuilder filterCategory(Long sellerId, Integer category) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(product.deleted.eq(false));
        builder.and(product.user.id.eq(sellerId));
        if(category != null) {
            builder.and(product.category.id.eq(category)
                    .or(product.category.superCategory.id.eq(category)));
        }
        return builder;
    }
}
