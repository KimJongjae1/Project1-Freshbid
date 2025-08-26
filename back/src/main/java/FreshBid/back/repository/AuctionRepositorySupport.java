package FreshBid.back.repository;

import static FreshBid.back.entity.QAuction.auction;
import static FreshBid.back.entity.QProduct.product;
import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.QAuction;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class AuctionRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public AuctionRepositorySupport(JPAQueryFactory queryFactory) {
        super(Auction.class);
        this.queryFactory = queryFactory;
    }

    // Join Fetch를 사용하여 Auction과 연관된 Product를 한 번에 조회
    public Auction findByIdWithProductsAndUser(Long auctionId) {
        return queryFactory.selectFrom(auction)
            .leftJoin(auction.product, product).fetchJoin()
            .leftJoin(product.user, user).fetchJoin()
            .where(auction.id.eq(auctionId))
            .fetchOne();
    }

    public Page<Auction> searchAuctions(AuctionSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        BooleanBuilder whereClause = buildWhereClause(searchRequest);
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(searchRequest);

        // 검색 쿼리 - distinct를 사용하여 중복 제거
        JPAQuery<Auction> query = queryFactory
                .selectFrom(auction)
                .distinct()
                .leftJoin(auction.product, product)
                .leftJoin(product.user, user)
                .where(whereClause)
                .orderBy(orderSpecifier);

        // 페이징 적용하여 결과 조회
        List<Auction> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 개수 조회 - count 쿼리를 별도로 실행
        JPAQuery<Long> countQuery = queryFactory
                .select(auction.countDistinct())
                .from(auction)
                .leftJoin(auction.product, product)
                .leftJoin(product.user, user)
                .where(whereClause);

        Long total = countQuery.fetchOne();
        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanBuilder buildWhereClause(AuctionSearchRequestDto searchRequest) {
        BooleanBuilder whereClause = new BooleanBuilder();

        // 판매자 ID 필터 - Live를 통해 접근
        if (searchRequest.getSellerId() != null) {
            whereClause.and(auction.live.seller.id.eq(searchRequest.getSellerId()));
        }

        // 판매자 닉네임 검색 (부분 일치) - Live를 통해 접근
        if (StringUtils.hasText(searchRequest.getSellerNickname())) {
            whereClause.and(
                    auction.live.seller.nickname.containsIgnoreCase(searchRequest.getSellerNickname()));
        }

        // 상품 카테고리 필터 - Product 개발 완료 후 주석 해제 필요
        if (searchRequest.getCategoryId() != null) {
            BooleanBuilder catOrUpper = new BooleanBuilder();
            catOrUpper.or(product.category.id.eq(searchRequest.getCategoryId()));
            catOrUpper.or(product.category.superCategory.id.eq(searchRequest.getCategoryId()));
            
            whereClause.and(catOrUpper);
        }

        // 상품명 검색 (부분 일치) - Product 개발 완료 후 주석 해제 필요
        if (StringUtils.hasText(searchRequest.getProductName())) {
            whereClause.and(
                    auction.product.name.containsIgnoreCase(searchRequest.getProductName()));
        }

        // 시작 가격 범위 필터
        if (searchRequest.getStartPriceFrom() != null) {
            whereClause.and(auction.startPrice.goe(searchRequest.getStartPriceFrom()));
        }
        if (searchRequest.getStartPriceTo() != null) {
            whereClause.and(auction.startPrice.loe(searchRequest.getStartPriceTo()));
        }

        // 수량 범위 필터
        if (searchRequest.getAmountFrom() != null) {
            whereClause.and(auction.amount.goe(searchRequest.getAmountFrom()));
        }
        if (searchRequest.getAmountTo() != null) {
            whereClause.and(auction.amount.loe(searchRequest.getAmountTo()));
        }

        // 경매 상태 필터 (다중 선택)
        if (searchRequest.getStatuses() != null && !searchRequest.getStatuses().isEmpty()) {
            whereClause.and(auction.status.in(searchRequest.getStatuses()));
        }

        return whereClause;
    }

    private OrderSpecifier<?> buildOrderSpecifier(AuctionSearchRequestDto searchRequest) {
        String sortBy = searchRequest.getSortBy();
        String sortDirection = searchRequest.getSortDirection();
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        switch (sortBy) {
            case "startPrice":
                return isAsc ? auction.startPrice.asc() : auction.startPrice.desc();
            case "amount":
                return isAsc ? auction.amount.asc() : auction.amount.desc();
            case "sellerNickName":
                return isAsc ? auction.live.seller.nickname.asc()
                        : auction.live.seller.nickname.desc();
            case "productName":
                return isAsc ? auction.product.name.asc() : auction.product.name.desc();
            case "status":
            default:
                return isAsc ? auction.status.asc() : auction.status.desc();
        }
    }
}
