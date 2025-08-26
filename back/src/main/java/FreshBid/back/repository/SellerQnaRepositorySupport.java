package FreshBid.back.repository;

import static FreshBid.back.entity.QSellerQna.sellerQna;
import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.SellerQna.QnaResponseDto;
import FreshBid.back.dto.SellerQna.SellerQnaResponseDto;
import FreshBid.back.dto.SellerQna.SellerQnaSearchRequestDto;
import FreshBid.back.entity.SellerQna;
import FreshBid.back.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SellerQnaRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public Page<SellerQna> search(SellerQnaSearchRequestDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());

        BooleanBuilder where = new BooleanBuilder();

        // 판매자 필터링
        if (dto.getSellerId() != null) {
            where.and(sellerQna.seller.id.eq(dto.getSellerId()));
        }

        // 최상위 문의만 조회 (답변 제외)
        where.and(sellerQna.superQna.isNull());

        // 키워드 검색
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            where.and(sellerQna.content.containsIgnoreCase(dto.getKeyword()));
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(dto.getSortBy(), dto.getSortDirection());

        // ✅ 별칭 충돌 해결: 각각 QUser 별도로 선언
        QUser sellerUser = new QUser("sellerUser");
        QUser writerUser = new QUser("writerUser");

        List<SellerQna> content = queryFactory
                .selectFrom(sellerQna)
                .leftJoin(sellerQna.seller, sellerUser).fetchJoin()
                .leftJoin(sellerQna.writer, writerUser).fetchJoin()
                .where(where.and(sellerQna.isDeleted.isFalse()))
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(sellerQna.count())
                .from(sellerQna)
                .where(where.and(sellerQna.isDeleted.isFalse()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    //QNA 게시글 댓글과 함께 조회
    public Page<QnaResponseDto> searchBySellerId(Long sellerId, Pageable pageable) {
        // 부모 QnA 조회 (페이지네이션 적용)
        List<QnaResponseDto> parents = queryFactory
                .select(Projections.fields(QnaResponseDto.class,
                        sellerQna.id,
                        Projections.fields(QnaResponseDto.WriterInfoDto.class,
                                sellerQna.writer.id,
                                sellerQna.writer.username,
                                sellerQna.writer.nickname
                        ).as("writer"),
                        sellerQna.content,
                        sellerQna.createdAt
                ))
                .from(sellerQna)
                .join(sellerQna.writer, user)
                .where(
                        sellerQna.seller.id.eq(sellerId)
                                .and(sellerQna.superQna.isNull())
                )
                .orderBy(sellerQna.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (parents.isEmpty()) {
            return new PageImpl<>(parents, pageable, 0);
        }

        // 부모 ID 목록
        List<Long> parentIds = parents.stream()
                .map(QnaResponseDto::getId)
                .collect(Collectors.toList());

        // 자식 QnA 조회 (같은 부모라면 작성 시간 빠른 순서)
        List<QnaResponseDto> children = queryFactory
                .select(Projections.fields(QnaResponseDto.class,
                        sellerQna.id,
                        Projections.fields(QnaResponseDto.WriterInfoDto.class,
                                sellerQna.writer.id,
                                sellerQna.writer.username,
                                sellerQna.writer.nickname
                        ).as("writer"),
                        sellerQna.content,
                        sellerQna.superQna.id.as("parentId"),
                        sellerQna.createdAt
                ))
                .from(sellerQna)
                .join(sellerQna.writer, user)
                .where(
                        sellerQna.seller.id.eq(sellerId)
                                .and(sellerQna.superQna.id.in(parentIds))
                )
                .orderBy(sellerQna.superQna.id.asc(), sellerQna.createdAt.asc())
                .fetch();

        // 부모 ID → 자식 리스트 매핑
        Map<Long, List<QnaResponseDto>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(QnaResponseDto::getParentId));

        // 부모 DTO에 자식 넣기
        parents.forEach(parent ->
                parent.setReplies(childrenMap.getOrDefault(parent.getId(), new ArrayList<>()))
        );

        // 전체 부모 QnA 개수
        long totalCount = queryFactory
                .select(sellerQna.count())
                .from(sellerQna)
                .where(
                        sellerQna.seller.id.eq(sellerId)
                                .and(sellerQna.superQna.isNull())
                )
                .fetchOne();

        return new PageImpl<>(parents, pageable, totalCount);
    }


    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        switch (sortBy) {
            case "createdAt":
            default:
                return isAsc ? sellerQna.createdAt.asc() : sellerQna.createdAt.desc();
        }
    }
}
