package FreshBid.back.repository;

import static FreshBid.back.entity.QAuction.auction;
import static FreshBid.back.entity.QLive.live;
import static FreshBid.back.entity.QProduct.product;
import static FreshBid.back.entity.QProductCategory.productCategory;
import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.live.LiveSearchRequestDto;
import FreshBid.back.dto.live.SellerLiveFilterRequestDto;
import FreshBid.back.entity.Live;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * QueryDSL 사용 기본적인 save, findById와 같은 jpaRepository에 없는 조회 쿼리 함수 작성 시 사용
 */
@Repository
public class LiveRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public LiveRepositorySupport(JPAQueryFactory queryFactory) {
        super(Live.class);
        this.queryFactory = queryFactory;
    }

    // Join Fetch를 사용하여 Live와 연관된 Auction, Product를 한 번에 조회
    public Live findByIdWithAuctionsAndProducts(Long liveId) {
        return queryFactory.selectFrom(live)
            .leftJoin(live.auctions, auction).fetchJoin()
            .leftJoin(auction.product, product).fetchJoin()
            .where(live.id.eq(liveId))
            .where(live.isDeleted.eq(false))
            .fetchOne();
    }

    /**
     * Live 검색 및 페이징 조회 판매자 정보를 포함하여 조회하기 위해 auction과 user를 조인
     */
    public Page<Live> searchLives(LiveSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        BooleanBuilder whereClause = buildWhereClause(searchRequest);
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(searchRequest);

        // 검색 쿼리 - distinct를 사용하여 중복 제거
        JPAQuery<Live> query = queryFactory
            .selectFrom(live)
            .distinct()
            .leftJoin(live.auctions, auction).fetchJoin()
            .leftJoin(auction.product, product).fetchJoin()
            .leftJoin(product.user, user).fetchJoin()
            .leftJoin(product.category, productCategory).fetchJoin()
            .where(whereClause)
            .where(live.isDeleted.eq(false))
            .orderBy(orderSpecifier);

        // 페이징 적용하여 결과 조회
        List<Live> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 총 개수 조회 - count 쿼리를 별도로 실행
        JPAQuery<Long> countQuery = queryFactory
            .select(live.countDistinct())
            .from(live)
            .leftJoin(live.auctions, auction)
            .leftJoin(auction.product, product)
            .leftJoin(product.user, user)
            .where(whereClause)
            .where(live.isDeleted.eq(false));

        Long total = countQuery.fetchOne();
        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    public Page<LiveBasicResponseDto> searchSellerLives(SellerLiveFilterRequestDto filterRequest) {
        Pageable pageable = PageRequest.of(filterRequest.getPageNo(), filterRequest.getPageSize());

        List<LiveBasicResponseDto> lives = queryFactory
                .select(Projections.fields(LiveBasicResponseDto.class,
                        live.id, live.reprImgSrc, live.title,live.startDate,live.endDate, live.status
                        ))
                .from(live)
                .where(filterLive(filterRequest))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(live.startDate.desc())
                .fetch();

        Long count = queryFactory
                .select(live.countDistinct())
                .from(live)
                .where(filterLive(filterRequest))
                .fetchOne();

        return new PageImpl<>(lives, pageable, count);
    }

    private BooleanBuilder filterLive(SellerLiveFilterRequestDto filterRequest) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(live.seller.id.eq(filterRequest.getSellerId()));
        builder.and(live.isDeleted.eq(false));

         if(!filterRequest.getIsEnded()) {
             builder.and(live.status.eq(Live.LiveStatus.IN_PROGRESS)
                     .or(live.status.eq(Live.LiveStatus.SCHEDULED)));
         }

        //제목 필터 추가
        if(filterRequest.getTitle() != null) {
            builder.and(live.title.contains(filterRequest.getTitle()));
        }
        return builder;
    }

    /**
     * 검색 조건을 BooleanBuilder로 구성
     */
    private BooleanBuilder buildWhereClause(LiveSearchRequestDto searchRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        // 판매자 ID 필터
        if (searchRequest.getSellerId() != null) {
            builder.and(live.seller.id.eq(searchRequest.getSellerId()));
        }

        // 판매자 닉네임 검색 (부분 일치)
        if (StringUtils.hasText(searchRequest.getSellerNickname())) {
            builder.and(live.seller.nickname.containsIgnoreCase(searchRequest.getSellerNickname()));
        }

        // 상품 카테고리 필터
        if (searchRequest.getCategoryId() != null) {
            BooleanBuilder catOrUpper = new BooleanBuilder();
            catOrUpper.or(product.category.id.eq(searchRequest.getCategoryId()));
            catOrUpper.or(product.category.superCategory.id.eq(searchRequest.getCategoryId()));

            builder.and(catOrUpper);
        }

        // 상품명 검색 (부분 일치)
        if (StringUtils.hasText(searchRequest.getProductName())) {
            builder.and(auction.product.name.containsIgnoreCase(searchRequest.getProductName()));
        }

        // 라이브 시작일 범위 필터
        if (searchRequest.getStartDateFrom() != null) {
            builder.and(live.startDate.goe(searchRequest.getStartDateFrom()));
        }
        if (searchRequest.getStartDateTo() != null) {
            builder.and(live.startDate.loe(searchRequest.getStartDateTo()));
        }

        // 라이브 종료일 범위 필터
        if (searchRequest.getEndDateFrom() != null) {
            builder.and(live.endDate.goe(searchRequest.getEndDateFrom()));
        }
        if (searchRequest.getEndDateTo() != null) {
            builder.and(live.endDate.loe(searchRequest.getEndDateTo()));
        }

        // 라이브 상태 필터 (여러개 가능)
        if (searchRequest.getStatuses() != null && !searchRequest.getStatuses().isEmpty()) {
            builder.and(live.status.in(searchRequest.getStatuses()));
        }

        // 라이브 제목 검색 (개별)
        if (StringUtils.hasText(searchRequest.getTitle())) {
            builder.and(live.title.containsIgnoreCase(searchRequest.getTitle()));
        }

        // 통합 OR 검색 (제목, 상품명, 판매자명)
        if (StringUtils.hasText(searchRequest.getSearchQuery())) {
            BooleanBuilder orBuilder = new BooleanBuilder();
            orBuilder.or(live.title.containsIgnoreCase(searchRequest.getSearchQuery()));
            orBuilder.or(auction.product.name.containsIgnoreCase(searchRequest.getSearchQuery()));
            orBuilder.or(live.seller.nickname.containsIgnoreCase(searchRequest.getSearchQuery()));
            builder.and(orBuilder);
        }

        return builder;
    }

    /**
     * 정렬 조건을 OrderSpecifier로 구성
     */
    private OrderSpecifier<?> buildOrderSpecifier(LiveSearchRequestDto searchRequest) {
        String sortBy = searchRequest.getSortBy();
        String sortDirection = searchRequest.getSortDirection();
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        switch (sortBy) {
            case "startDate":
                return isAsc ? live.startDate.asc() : live.startDate.desc();
            case "title":
                return isAsc ? live.title.asc() : live.title.desc();
            case "endDate":
            default:
                return isAsc ? live.endDate.asc() : live.endDate.desc();
        }
    }

    public List<Live> findExpiredLives(LocalDateTime today) {
        return queryFactory.selectFrom(live)
                .where(live.endDate.before(today))
                .fetch();
    }
}
