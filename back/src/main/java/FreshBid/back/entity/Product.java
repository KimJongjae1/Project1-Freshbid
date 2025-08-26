package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자 (user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 카테고리 (category_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "origin", nullable = false, length = 100)
    private String origin = "";

    @Builder.Default
    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight = BigDecimal.valueOf(1.0);

    @Column(name = "repr_img_src")
    private String reprImgSrc;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;

    public enum Grade {
        특, 상, 중, 하
    }

    /**
     * DTO → Entity 변환 헬퍼
     */
    public static Product of(FreshBid.back.dto.product.ProductCreateRequestDto dto, String imgFilePath,
                             User user, ProductCategory category) {
        return Product.builder()
                .user(user)
                .category(category)
                .name(dto.getName())
                .origin(dto.getOrigin())
                .weight(dto.getWeight())
                .reprImgSrc(imgFilePath)
                .description(dto.getDescription())
                .grade(dto.getGrade())
                .build();
    }
}
