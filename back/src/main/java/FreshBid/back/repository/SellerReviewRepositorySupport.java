package FreshBid.back.repository;

import static FreshBid.back.entity.QSellerReview.sellerReview;
import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.SellerReview.SellerReviewSearchRequestDto;
import FreshBid.back.entity.SellerReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerReviewRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public Page<SellerReview> search(SellerReviewSearchRequestDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());

        BooleanBuilder where = new BooleanBuilder();
        if (dto.getSellerId() != null) {
            where.and(sellerReview.seller.id.eq(dto.getSellerId()));
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(dto.getSortBy(), dto.getSortDirection());

        List<SellerReview> content = queryFactory
                .selectFrom(sellerReview)
                .leftJoin(sellerReview.user, user).fetchJoin()
                .where(where)  // 삭제된 리뷰도 포함하여 조회
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(sellerReview.count())
                .from(sellerReview)
                .where(where)  // 삭제된 리뷰도 포함하여 카운트
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        switch (sortBy) {
            case "rate":
                return isAsc ? sellerReview.rate.asc() : sellerReview.rate.desc();
            case "createdAt":
            default:
                return isAsc ? sellerReview.createdAt.asc() : sellerReview.createdAt.desc();
        }
    }
}
