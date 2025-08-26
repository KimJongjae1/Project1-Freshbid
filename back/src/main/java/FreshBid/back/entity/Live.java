package FreshBid.back.entity;

import FreshBid.back.dto.live.LiveCreateRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "live")
@Getter
@Setter
@NoArgsConstructor
public class Live {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User seller;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotNull
    @ColumnDefault("'SCHEDULED'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LiveStatus status;
    
    @Column(name = "repr_img_src")
    private String reprImgSrc;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @OneToMany(mappedBy = "live", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Auction> auctions = new ArrayList<>();

    public void addAuction(Auction auction) {
        this.auctions.add(auction);
        auction.setLive(this);
    }

    public static Live of(LiveCreateRequestDto liveCreateRequestDto) {
        Live live = new Live();
        
        live.title = liveCreateRequestDto.getTitle();
        live.startDate = liveCreateRequestDto.getStartDate();
        live.endDate = liveCreateRequestDto.getEndDate();
        live.status = LiveStatus.SCHEDULED;

        return live;
    }

    //라이브의 진행 상태 필터 enum
    public enum LiveStatus {
        SCHEDULED("예정됨"),
        IN_PROGRESS("진행중"),
        ENDED("종료됨");
        private String description;

        LiveStatus(String description) {
            this.description = description;
        }
    }
}
