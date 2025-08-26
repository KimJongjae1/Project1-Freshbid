package FreshBid.back.repository;

import static FreshBid.back.entity.QAuction.auction;
import static FreshBid.back.entity.QAuctionHistory.auctionHistory;
import static FreshBid.back.entity.QOrder.order;
import static FreshBid.back.entity.QProduct.product;
import static FreshBid.back.entity.QProductCategory.productCategory;
import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.cart.CartProductDto;
import FreshBid.back.entity.Order;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public OrderRepositorySupport(JPAQueryFactory queryFactory) {
        super(Order.class);
        this.queryFactory = queryFactory;
    }

    public Order findByIdWithDetail(Long orderId) {

        return queryFactory
            .selectFrom(order)
            .join(order.auctionHistory, auctionHistory).fetchJoin()
            .join(order.auction, auction).fetchJoin()
            .join(auction.product, product).fetchJoin()
            .join(product.user, user).fetchJoin()
            .join(product.category, productCategory).fetchJoin()
            .where(
                order.id.eq(orderId)
            )
            .orderBy(order.createdAt.desc())
            .fetchOne();
    }

    public List<Order> findByAuctionIdWithAuctionAndHistoryAndSeller(Long auctionId) {
        return queryFactory
            .selectFrom(order)
            .join(order.auction, auction).fetchJoin()       // Dto에서 사용
            .join(order.auctionHistory, auctionHistory).fetchJoin() // Dto에서 사용
            .join(order.seller, user).fetchJoin()       // Service에서 조회 권한으로 인해 사용
            .where(auction.id.eq(auctionId))
            .fetch();
    }

    public List<Order> findByCustomerIdWithAuctionAndHistoryAndSeller(Long customerId) {
        return queryFactory
            .selectFrom(order)
            .join(order.auction, auction).fetchJoin()       // Dto에서 사용
            .join(order.auctionHistory, auctionHistory).fetchJoin() // Dto에서 사용
            .join(order.seller, user).fetchJoin()       // Service에서 조회 권한으로 인해 사용
            .where(order.customer.id.eq(customerId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    public List<Order> findBySellerIdWithAuctionAndHistoryAndSeller(Long sellerId) {
        return queryFactory
            .selectFrom(order)
            .join(order.auction, auction).fetchJoin()       // Dto에서 사용
            .join(order.auctionHistory, auctionHistory).fetchJoin() // Dto에서 사용
            .join(order.seller, user).fetchJoin()       // Service에서 조회 권한으로 인해 사용
            .where(order.seller.id.eq(sellerId))
            .orderBy(order.createdAt.desc())
            .fetch();
    }

    public List<Order> findAllWithAuctionAndHistoryAndSeller() {
        return queryFactory
            .selectFrom(order)
            .join(order.auction, auction).fetchJoin()       // Dto에서 사용
            .join(order.auctionHistory, auctionHistory).fetchJoin() // Dto에서 사용
            .join(order.seller, user).fetchJoin()       // Service에서 조회 권한으로 인해 사용
            .orderBy(order.createdAt.desc())
            .fetch();
    }
    
    public List<CartProductDto> findAllOrderByUserId(Long userId) {
        return queryFactory
            .select(Projections.constructor(
                CartProductDto.class,
                product.name,
                product.reprImgSrc,
                product.weight,
                auction.amount,
                user.nickname,
                product.origin,
                auctionHistory.price,
                order.status,
                order.createdAt
            ))
            .from(order)
            .join(order.auction, auction)
            .join(auction.product, product)
            .join(order.seller, user)
            .where(
                order.customer.id.eq(userId)
            )
            .orderBy(order.createdAt.desc())
            .fetch();
    }


}