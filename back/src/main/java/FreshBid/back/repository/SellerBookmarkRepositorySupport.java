package FreshBid.back.repository;

import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.entity.SellerBookmark;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static FreshBid.back.entity.QSellerBookmark.sellerBookmark;
import static FreshBid.back.entity.QUser.user;

@Repository
public class SellerBookmarkRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public SellerBookmarkRepositorySupport(JPAQueryFactory queryFactory) {
        super(SellerBookmark.class);
        this.queryFactory = queryFactory;
    }

    public List<SellerBasicInfoDto> findByUserId(Long userId) {
        return queryFactory
                .select(Projections.fields(SellerBasicInfoDto.class,
                        sellerBookmark.seller.id,
                        sellerBookmark.seller.profileImage,
                        sellerBookmark.seller.username,
                        sellerBookmark.seller.nickname,
                        sellerBookmark.seller.address,
                        sellerBookmark.seller.phoneNumber,
                        sellerBookmark.seller.introduction
                        ))
                .from(sellerBookmark)
                .leftJoin(sellerBookmark.seller, user)
                .where(sellerBookmark.user.id.eq(userId))
                .fetch();
    }

    public Long getSellerBookmarkCountById(Long sellerId) {
        return queryFactory
                .select(sellerBookmark.count())
                .from(sellerBookmark)
                .where(sellerBookmark.seller.id.eq(sellerId))
                .fetchOne();
    }
}
