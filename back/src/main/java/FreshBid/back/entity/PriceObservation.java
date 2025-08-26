package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_observation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false)
    private ProductCategory itemCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private Product.Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "observed_at", nullable = false)
    private LocalDate observedAt;

    @Column(name = "price_per_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal pricePerKg;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Source {
        INTERNAL, EXTERNAL
    }
}
