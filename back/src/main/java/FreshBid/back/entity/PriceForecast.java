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
@Table(name = "price_forecast")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false)
    private ProductCategory itemCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private Product.Grade grade;

    @Column(name = "ds", nullable = false)
    private LocalDate ds;

    @Column(name = "yhat", nullable = false, precision = 12, scale = 3)
    private BigDecimal yhat;

    @Column(name = "yhat_lower", precision = 12, scale = 3)
    private BigDecimal yhatLower;

    @Column(name = "yhat_upper", precision = 12, scale = 3)
    private BigDecimal yhatUpper;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
