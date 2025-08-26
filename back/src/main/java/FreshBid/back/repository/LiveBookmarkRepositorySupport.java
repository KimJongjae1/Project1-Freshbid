package FreshBid.back.repository;

import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.entity.LiveBookmark;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static FreshBid.back.entity.QLive.live;
import static FreshBid.back.entity.QLiveBookmark.liveBookmark;

@Repository
public class LiveBookmarkRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public LiveBookmarkRepositorySupport(JPAQueryFactory queryFactory) {
        super(LiveBookmark.class);
        this.queryFactory = queryFactory;
    }

    public List<LiveBasicResponseDto> findByUserId(Long userId) {
        List<LiveBasicResponseDto> lives = queryFactory
                .select(Projections.fields(LiveBasicResponseDto.class,
                        liveBookmark.live.id,
                        liveBookmark.live.reprImgSrc,
                        liveBookmark.live.title,
                        liveBookmark.live.startDate,
                        liveBookmark.live.endDate,
                        liveBookmark.live.status
                ))
                .from(liveBookmark)
                .leftJoin(liveBookmark.live, live)
                .where(liveBookmark.user.id.eq(userId))
                .fetch();

        return lives;
    }
}
