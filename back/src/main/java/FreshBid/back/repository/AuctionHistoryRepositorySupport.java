package FreshBid.back.repository;

import FreshBid.back.entity.Auction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class AuctionHistoryRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public AuctionHistoryRepositorySupport(JPAQueryFactory queryFactory) {
        super(Auction.class);
        this.queryFactory = queryFactory;
    }

}
