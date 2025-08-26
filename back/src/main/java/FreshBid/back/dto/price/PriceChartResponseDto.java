package FreshBid.back.dto.price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChartResponseDto {
    
    private String itemName;
    private String grade;
    private BigDecimal currentPrice;
    private LocalDate lastUpdate;
    
    private List<PriceDataPoint> actualData;
    private List<PriceDataPoint> forecastData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDataPoint {
        private LocalDate date;
        private BigDecimal price;
    }
}
