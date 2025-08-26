package FreshBid.back.entity;

import FreshBid.back.dto.SellerQna.SellerQnaCreateRequestDto;
import FreshBid.back.dto.SellerQna.SellerQnaUpdateRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_qna")
@Getter
@NoArgsConstructor
public class SellerQna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // 판매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer; // 작성자 (구매자 또는 판매자)

    // 대댓글 (답변)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_id")
    private SellerQna superQna;

    @OneToMany(mappedBy = "superQna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SellerQna> replies = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static SellerQna from(SellerQnaCreateRequestDto dto, User writer, User seller, SellerQna superQna) {
        return SellerQna.of(writer, seller, dto.getContent(), superQna);
    }

    public static SellerQna of(User writer, User seller, String content, SellerQna superQna) {
        SellerQna qna = new SellerQna();
        qna.writer = writer;
        qna.seller = seller;
        qna.content = content;
        qna.superQna = superQna;
        return qna;
    }

    public void update(SellerQnaUpdateRequestDto dto) {
        this.content = dto.getContent();
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
} 