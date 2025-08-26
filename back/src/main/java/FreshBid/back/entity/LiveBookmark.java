package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="live_bookmark")
@Getter
@NoArgsConstructor
public class LiveBookmark {

    @EmbeddedId
    private LiveBookmarkKey key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "live_id", nullable = false)
    @MapsId("liveId")
    private Live live;

    //찜한 소비자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "user_id", nullable = false)
    @MapsId("userId")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static LiveBookmark of(Live live, User user) {
        LiveBookmark bookmark = new LiveBookmark();

        bookmark.live = live;
        bookmark.user = user;

        // 복합 키 생성
        bookmark.key = new LiveBookmarkKey(live.getId(), user.getId());

        // createdAt은 @PrePersist에서 자동 세팅됨
        return bookmark;
    }
}
