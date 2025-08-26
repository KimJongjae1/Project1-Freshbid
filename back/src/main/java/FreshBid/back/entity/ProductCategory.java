package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 상위 카테고리 (nullable)
    @ManyToOne
    @JoinColumn(name = "upper_id")
    private ProductCategory superCategory;

    @Column(length = 20)
    private String name;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public static ProductCategory of(String name, ProductCategory superCategory) {
        return ProductCategory.builder()
                .name(name)
                .superCategory(superCategory)
                .isDeleted(false)
                .build();
    }
}
