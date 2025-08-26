package FreshBid.back.repository;

import static FreshBid.back.entity.QUser.user;

import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.dto.user.UserSearchResponseDto;
import FreshBid.back.entity.User;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public UserRepositorySupport(JPAQueryFactory queryFactory) {
        super(User.class);
        this.queryFactory = queryFactory;
    }

    public User findByUsername(String username) {
        return queryFactory.selectFrom(user)
                .where(user.username.eq(username))
                .fetchOne();
    }

    public User findById(Long userId) {
        return queryFactory.selectFrom(user)
                .where(user.id.eq(
                        userId))
                .fetchOne();
    }

    public User findByNickname(String nickname) {
        return queryFactory.selectFrom(user)
                .where(user.nickname.eq(nickname))
                .fetchOne();
    }

    public Optional<SellerBasicInfoDto> getSellerBasicInfoById(Long userId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.fields(SellerBasicInfoDto.class,
                        user.id,
                        user.profileImage,
                        user.username,
                        user.nickname,
                        user.address,
                        user.phoneNumber,
                        user.introduction
                ))
                .from(user)
                .where(user.id.eq(userId).and(user.role.eq(User.Role.ROLE_SELLER)))
                .fetchOne());
    }

    public Optional<User> getSellerById(Long userId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(user)
                .where(user.id.eq(userId).and(user.role.eq(User.Role.ROLE_SELLER)))
                .fetchOne());
    }

    public List<UserSearchResponseDto> searchSellerByQuery(String searchQuery) {
        //searchQuery가 포함된 nickname, username의 판매자들을 조회합니다.
        return queryFactory.select(Projections.fields(UserSearchResponseDto.class,
                    user.id, user.profileImage, user.username, user.nickname
                ))
                .from(user)
                .where(user.role.eq(User.Role.ROLE_SELLER)
                        .and(user.nickname.contains(searchQuery).or(user.username.contains(searchQuery))))
                .fetch();
    }
}
