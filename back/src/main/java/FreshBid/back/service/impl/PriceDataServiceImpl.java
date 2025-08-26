package FreshBid.back.service.impl;

import FreshBid.back.dto.price.PriceChartResponseDto;
import FreshBid.back.dto.price.PriceDataRequestDto;
import FreshBid.back.entity.*;
import FreshBid.back.repository.PriceForecastRepository;
import FreshBid.back.repository.PriceObservationRepository;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.service.PriceDataService;
import FreshBid.back.service.CategoryCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDataServiceImpl implements PriceDataService {

    private final PriceObservationRepository priceObservationRepository;
    private final PriceForecastRepository priceForecastRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryCacheService categoryCacheService;

    @Override
    @Transactional(readOnly = true)
    public PriceChartResponseDto getPriceChartData(Long categoryId, String grade) {
        log.info("가격 차트 데이터 조회 - 카테고리ID: {}, 등급: {}", categoryId, grade);

        // 카테고리 정보 조회
        ProductCategory category = productCategoryRepository.findById(categoryId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));

        // 등급 enum 변환 시도
        Product.Grade gradeEnum;
        try {
            gradeEnum = Product.Grade.valueOf(grade);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 등급 값: {}", grade);
            throw new IllegalArgumentException("잘못된 등급입니다: " + grade);
        }

        // 실제 가격 데이터 조회 (최근 3년)
        LocalDate startDate = LocalDate.now().minusYears(3);
        LocalDate endDate = LocalDate.now();
        List<PriceObservation> actualData = priceObservationRepository
                .findByCategoryAndGradeAndDateRange(categoryId, gradeEnum, startDate);

        // 데이터가 없는 경우 처리
        if (actualData.isEmpty()) {
            log.warn("가격 데이터가 없습니다 - 카테고리ID: {}, 등급: {}", categoryId, grade);
            throw new IllegalArgumentException(
                String.format("카테고리 ID %d의 %s 등급 데이터가 없습니다. 사용 가능한 등급을 확인해주세요.", 
                    categoryId, grade)
            );
        }

        // 예측 데이터 조회 (3년 전부터 90일 후까지의 예측 데이터)
        List<PriceForecast> allForecastData = priceForecastRepository
                .findByCategoryAndGradeOrderByDateDesc(categoryId, gradeEnum);
        
        // 3년 전부터 90일 후까지의 예측 데이터 필터링
        LocalDate forecastEndDate = LocalDate.now().plusDays(90);
        List<PriceForecast> forecastData = allForecastData.stream()
                .filter(data -> !data.getDs().isBefore(startDate) && !data.getDs().isAfter(forecastEndDate))
                .collect(Collectors.toList());
        
        log.info("예측 데이터 조회 결과 - 카테고리ID: {}, 등급: {}, 데이터 개수: {}", 
                categoryId, grade, forecastData.size());
        
        if (!forecastData.isEmpty()) {
            log.info("예측 데이터 범위 - 시작: {}, 끝: {}", 
                    forecastData.get(forecastData.size() - 1).getDs(), 
                    forecastData.get(0).getDs());
        }

        // 최신 실제 가격
        BigDecimal currentPrice = actualData.isEmpty() ? BigDecimal.ZERO : 
                actualData.get(actualData.size() - 1).getPricePerKg();
        LocalDate lastUpdate = actualData.isEmpty() ? LocalDate.now() : 
                actualData.get(actualData.size() - 1).getObservedAt();

        // DTO 변환
        List<PriceChartResponseDto.PriceDataPoint> actualPoints = actualData.stream()
                .map(data -> PriceChartResponseDto.PriceDataPoint.builder()
                        .date(data.getObservedAt())
                        .price(data.getPricePerKg())
                        .build())
                .collect(Collectors.toList());

        List<PriceChartResponseDto.PriceDataPoint> forecastPoints = forecastData.stream()
                .sorted((a, b) -> a.getDs().compareTo(b.getDs())) // 날짜순 정렬
                .map(data -> PriceChartResponseDto.PriceDataPoint.builder()
                        .date(data.getDs())
                        .price(data.getYhat())
                        .build())
                .collect(Collectors.toList());

        return PriceChartResponseDto.builder()
                .itemName(category.getName())
                .grade(grade)
                .currentPrice(currentPrice)
                .lastUpdate(lastUpdate)
                .actualData(actualPoints)
                .forecastData(forecastPoints)
                .build();
    }

    @Override
    @Transactional
    public void addPriceDataFromOrder(Order order) {
        log.info("주문 완료로부터 가격 데이터 추가 - 주문ID: {}", order.getId());

        Auction auction = order.getAuction();
        Product product = auction.getProduct();
        
        // kg 단위로 가격 계산
        BigDecimal pricePerKg = calculatePricePerKg(order.getPrice(), product.getWeight());
        
        // 오늘 날짜로 가격 데이터 추가
        LocalDate today = LocalDate.now();
        
        // 중복 데이터 확인
        priceObservationRepository.findByCategoryAndGradeAndDate(
                product.getCategory().getId().longValue(), 
                product.getGrade(), 
                today
        ).ifPresentOrElse(
            existing -> {
                // 기존 데이터가 있으면 평균값으로 업데이트
                BigDecimal avgPrice = existing.getPricePerKg().add(pricePerKg)
                        .divide(BigDecimal.valueOf(2), 3, RoundingMode.HALF_UP);
                existing.setPricePerKg(avgPrice);
                priceObservationRepository.save(existing);
                log.info("기존 가격 데이터 업데이트 - 카테고리: {}, 등급: {}, 가격: {}", 
                        product.getCategory().getName(), product.getGrade(), avgPrice);
            },
            () -> {
                // 새 데이터 생성
                PriceObservation newObservation = PriceObservation.builder()
                        .source(PriceObservation.Source.INTERNAL)
                        .itemCategory(product.getCategory())
                        .grade(product.getGrade())
                        .product(product)
                        .observedAt(today)
                        .pricePerKg(pricePerKg)
                        .build();
                priceObservationRepository.save(newObservation);
                log.info("새 가격 데이터 추가 - 카테고리: {}, 등급: {}, 가격: {}", 
                        product.getCategory().getName(), product.getGrade(), pricePerKg);
            }
        );

        // 임계값 기반 실시간 예측 업데이트
        if (isSignificantPriceChange(product.getCategory().getId().longValue(), 
                                   product.getGrade().name(), pricePerKg)) {
            log.info("중요한 가격 변동 감지 - 실시간 예측 업데이트 실행");
            generateForecastData(product.getCategory().getId().longValue(), product.getGrade().name());
        }
    }

    @Override
    @Transactional
    public void generateForecastData(Long categoryId, String grade) {
        log.info("예측 데이터 생성 시작 - 카테고리ID: {}, 등급: {}", categoryId, grade);

        // 실제 데이터 조회 (최근 2년)
        LocalDate startDate = LocalDate.now().minusYears(2);
        Product.Grade gradeEnum = Product.Grade.valueOf(grade);
        List<PriceObservation> actualData = priceObservationRepository
                .findByCategoryAndGradeAndDateRange(categoryId, gradeEnum, startDate);

        if (actualData.size() < 10) {
            log.warn("예측을 위한 데이터가 부족합니다 - 카테고리ID: {}, 등급: {}, 데이터수: {}", 
                    categoryId, grade, actualData.size());
            return;
        }

        // 기존 예측 데이터 삭제
        List<PriceForecast> existingForecasts = priceForecastRepository
                .findByCategoryAndGradeOrderByDateDesc(categoryId, gradeEnum);
        priceForecastRepository.deleteAll(existingForecasts);

        try {
            // AI 서비스 API 호출하여 예측 데이터 생성
            // 개발환경: localhost, 운영환경: ai-service
            String aiServiceUrl = System.getenv("AI_SERVICE_URL") != null ? 
                System.getenv("AI_SERVICE_URL") + "/api/predictions/generate" :
                "http://localhost:5001/api/predictions/generate";
            
            // 실제 데이터를 AI 서비스 형식으로 변환
            List<PriceDataPoint> dataPoints = actualData.stream()
                    .map(data -> new PriceDataPoint(
                            data.getObservedAt().toString(),
                            data.getPricePerKg().doubleValue()
                    ))
                    .collect(Collectors.toList());

            // AI 서비스 요청 데이터
            ForecastRequest request = new ForecastRequest(categoryId, grade, dataPoints);
            
            // RestTemplate을 사용하여 AI 서비스 호출
            RestTemplate restTemplate = new RestTemplate();
            ForecastResponse response = restTemplate.postForObject(aiServiceUrl, request, ForecastResponse.class);
            
            if (response != null && response.getForecast() != null) {
                // AI 서비스에서 받은 예측 데이터를 DB에 저장
                for (ForecastData forecastData : response.getForecast()) {
                    PriceForecast forecast = PriceForecast.builder()
                            .itemCategory(actualData.get(0).getItemCategory())
                            .grade(actualData.get(0).getGrade())
                            .ds(LocalDate.parse(forecastData.getDate()))
                            .yhat(BigDecimal.valueOf(forecastData.getPrice()))
                            .yhatLower(BigDecimal.valueOf(forecastData.getLowerBound()))
                            .yhatUpper(BigDecimal.valueOf(forecastData.getUpperBound()))
                            .build();
                    
                    priceForecastRepository.save(forecast);
                }
                
                log.info("AI 서비스에서 예측 데이터 생성 완료 - 카테고리ID: {}, 등급: {}, 예측일수: {}", 
                        categoryId, grade, response.getForecast().size());
            } else {
                throw new RuntimeException("AI 서비스에서 예측 데이터를 받지 못했습니다.");
            }

            log.info("예측 데이터 생성 완료 - 카테고리ID: {}, 등급: {}", categoryId, grade);
            
        } catch (Exception e) {
            log.error("AI 서비스 호출 중 오류 발생 - 카테고리ID: {}, 등급: {}", categoryId, grade, e);
            // 오류 발생 시 기존 예측 데이터 유지
        }
    }

    /**
     * 가격 변동이 임계값을 초과하는지 확인
     */
    private boolean isSignificantPriceChange(Long categoryId, String grade, BigDecimal newPrice) {
        try {
            // 최근 7일 평균 가격 계산
            LocalDate weekAgo = LocalDate.now().minusDays(7);
            Product.Grade gradeEnum = Product.Grade.valueOf(grade);
            List<PriceObservation> recentData = priceObservationRepository
                    .findByCategoryAndGradeAndDateRange(categoryId, gradeEnum, weekAgo);
            
            if (recentData.size() < 3) {
                return false; // 데이터가 부족하면 업데이트 안함
            }
            
            BigDecimal avgPrice = recentData.stream()
                    .map(PriceObservation::getPricePerKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(recentData.size()), 3, RoundingMode.HALF_UP);
            
            // 변동률 계산
            double changeRate = Math.abs(newPrice.subtract(avgPrice).doubleValue() / avgPrice.doubleValue());
            
            log.debug("가격 변동률: {}% (임계값: 10%)", changeRate * 100);
            
            return changeRate > 0.1; // 10% 이상 변동 시
            
        } catch (Exception e) {
            log.error("가격 변동률 계산 중 오류", e);
            return false;
        }
    }

    // AI 서비스 요청/응답을 위한 내부 클래스
    private static class PriceDataPoint {
        private String date;
        private double price;
        
        public PriceDataPoint(String date, double price) {
            this.date = date;
            this.price = price;
        }
        
        // getters
        public String getDate() { return date; }
        public double getPrice() { return price; }
    }
    
    private static class ForecastRequest {
        private Long categoryId;
        private String grade;
        private List<PriceDataPoint> data;
        
        public ForecastRequest(Long categoryId, String grade, List<PriceDataPoint> data) {
            this.categoryId = categoryId;
            this.grade = grade;
            this.data = data;
        }
        
        // getters
        public Long getCategoryId() { return categoryId; }
        public String getGrade() { return grade; }
        public List<PriceDataPoint> getData() { return data; }
    }
    
    private static class ForecastResponse {
        private Long categoryId;
        private String grade;
        private List<ForecastData> forecast;
        
        public ForecastResponse(Long categoryId, String grade, List<ForecastData> forecast) {
            this.categoryId = categoryId;
            this.grade = grade;
            this.forecast = forecast;
        }
        
        // getters
        public Long getCategoryId() { return categoryId; }
        public String getGrade() { return grade; }
        public List<ForecastData> getForecast() { return forecast; }
    }
    
    private static class ForecastData {
        private String date;
        private double price;
        private double lowerBound;
        private double upperBound;
        
        public ForecastData(String date, double price, double lowerBound, double upperBound) {
            this.date = date;
            this.price = price;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
        
        // getters
        public String getDate() { return date; }
        public double getPrice() { return price; }
        public double getLowerBound() { return lowerBound; }
        public double getUpperBound() { return upperBound; }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getAvailableCategories() {
        log.info("사용 가능한 카테고리 목록 조회");

        return productCategoryRepository.findAll().stream()
                .filter(category -> category.getSuperCategory() != null) // 하위 카테고리만
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .superCategoryId(category.getSuperCategory().getId().longValue())
                        .superCategoryName(category.getSuperCategory().getName())
                        .grade("상") // 기본값, 실제로는 모든 등급을 반환해야 함
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getSuperCategories() {
        log.info("상위 카테고리 목록 조회");

        // Redis 캐시에서 먼저 조회
        List<PriceDataRequestDto> cachedCategories = categoryCacheService.getCachedSuperCategories();
        if (cachedCategories != null) {
            log.debug("Redis에서 상위 카테고리 조회 - {}개 항목", cachedCategories.size());
            return cachedCategories;
        }
        
        // 캐시에 없으면 DB에서 조회하고 캐시에 저장
        List<PriceDataRequestDto> categories = productCategoryRepository.findAll().stream()
                .filter(category -> category.getSuperCategory() == null) // 상위 카테고리만
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .grade("상")
                        .build())
                .collect(Collectors.toList());
        
        // Redis에 캐싱
        categoryCacheService.cacheSuperCategories(categories);
        
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getSubCategories(Long superCategoryId) {
        log.info("하위 카테고리 목록 조회 - 상위 카테고리ID: {}", superCategoryId);

        // Redis 캐시에서 먼저 조회
        List<PriceDataRequestDto> cachedCategories = categoryCacheService.getCachedSubCategories(superCategoryId);
        if (cachedCategories != null) {
            log.debug("Redis에서 하위 카테고리 조회 - 상위카테고리ID: {}, {}개 항목", superCategoryId, cachedCategories.size());
            return cachedCategories;
        }
        
        // 캐시에 없으면 DB에서 조회하고 캐시에 저장
        List<PriceDataRequestDto> categories = productCategoryRepository.findAll().stream()
                .filter(category -> category.getSuperCategory() != null && 
                        category.getSuperCategory().getId().longValue() == superCategoryId)
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .superCategoryId(category.getSuperCategory().getId().longValue())
                        .superCategoryName(category.getSuperCategory().getName())
                        .grade("상")
                        .build())
                .collect(Collectors.toList());
        
        // Redis에 캐싱
        categoryCacheService.cacheSubCategories(superCategoryId, categories);
        
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableGrades(Long categoryId) {
        log.info("사용 가능한 등급 목록 조회 - 카테고리ID: {}", categoryId);

        // Redis 캐시에서 먼저 조회
        List<String> cachedGrades = categoryCacheService.getCachedGrades(categoryId);
        if (cachedGrades != null) {
            log.debug("Redis에서 등급 조회 - 카테고리ID: {}, {}개 항목", categoryId, cachedGrades.size());
            return cachedGrades;
        }
        
        // 캐시에 없으면 DB에서 조회하고 캐시에 저장
        List<String> grades = priceObservationRepository.findByItemCategoryId(categoryId.intValue())
                .stream()
                .map(observation -> observation.getGrade().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 데이터가 없으면 기본 등급들 반환
        if (grades.isEmpty()) {
            grades = List.of("특", "상", "중", "하");
        }
        
        // Redis에 캐싱
        categoryCacheService.cacheGrades(categoryId, grades);
        
        return grades;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getAvailableCategoriesWithData() {
        log.info("데이터가 있는 카테고리 목록 조회");

        // 실제 데이터가 있는 카테고리 ID들 조회
        List<Integer> categoriesWithData = priceObservationRepository.findAll()
                .stream()
                .map(observation -> observation.getItemCategory().getId())
                .distinct()
                .collect(Collectors.toList());

        return productCategoryRepository.findAll().stream()
                .filter(category -> categoriesWithData.contains(category.getId()))
                .filter(category -> category.getSuperCategory() != null) // 하위 카테고리만
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .superCategoryId(category.getSuperCategory().getId().longValue())
                        .superCategoryName(category.getSuperCategory().getName())
                        .grade("상")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getSuperCategoriesWithData() {
        log.info("데이터가 있는 상위 카테고리 목록 조회");

        // 실제 데이터가 있는 카테고리 ID들 조회
        List<Integer> categoriesWithData = priceObservationRepository.findAll()
                .stream()
                .map(observation -> observation.getItemCategory().getId())
                .distinct()
                .collect(Collectors.toList());

        // 데이터가 있는 하위 카테고리들의 상위 카테고리 ID들 조회
        List<Integer> superCategoriesWithData = productCategoryRepository.findAll().stream()
                .filter(category -> categoriesWithData.contains(category.getId()))
                .filter(category -> category.getSuperCategory() != null)
                .map(category -> category.getSuperCategory().getId().intValue())
                .distinct()
                .collect(Collectors.toList());

        return productCategoryRepository.findAll().stream()
                .filter(category -> category.getSuperCategory() == null) // 상위 카테고리만
                .filter(category -> superCategoriesWithData.contains(category.getId()))
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .grade("상")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceDataRequestDto> getSubCategoriesWithData(Long superCategoryId) {
        log.info("데이터가 있는 하위 카테고리 목록 조회 - 상위 카테고리ID: {}", superCategoryId);

        // 실제 데이터가 있는 카테고리 ID들 조회
        List<Integer> categoriesWithData = priceObservationRepository.findAll()
                .stream()
                .map(observation -> observation.getItemCategory().getId())
                .distinct()
                .collect(Collectors.toList());

        return productCategoryRepository.findAll().stream()
                .filter(category -> category.getSuperCategory() != null && 
                        category.getSuperCategory().getId().longValue() == superCategoryId)
                .filter(category -> categoriesWithData.contains(category.getId()))
                .map(category -> PriceDataRequestDto.builder()
                        .categoryId(category.getId().longValue())
                        .categoryName(category.getName())
                        .superCategoryId(category.getSuperCategory().getId().longValue())
                        .superCategoryName(category.getSuperCategory().getName())
                        .grade("상")
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculatePricePerKg(Long totalPrice, BigDecimal weight) {
        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalPrice).divide(weight, 3, RoundingMode.HALF_UP);
    }

    /**
     * 일일 배치 예측 업데이트 (매일 새벽 2시 실행)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void dailyForecastUpdate() {
        log.info("일일 배치 예측 업데이트 시작");
        
        try {
            // 모든 카테고리와 등급 조합에 대해 예측 업데이트 실행
            List<ProductCategory> categories = productCategoryRepository.findAll()
                    .stream()
                    .filter(category -> category.getSuperCategory() != null) // 하위 카테고리만
                    .collect(Collectors.toList());
            
            int successCount = 0;
            int totalCount = 0;
            
            for (ProductCategory category : categories) {
                // 해당 카테고리의 모든 등급 조회
                List<String> grades = getAvailableGrades(category.getId().longValue());
                
                for (String grade : grades) {
                    try {
                        generateForecastData(category.getId().longValue(), grade);
                        successCount++;
                        log.debug("예측 업데이트 완료 - 카테고리: {}, 등급: {}", 
                                category.getName(), grade);
                    } catch (Exception e) {
                        log.error("예측 업데이트 실패 - 카테고리: {}, 등급: {}", 
                                category.getName(), grade, e);
                    }
                    totalCount++;
                }
            }
            
            log.info("일일 배치 예측 업데이트 완료 - 성공: {}/{}", successCount, totalCount);
            
        } catch (Exception e) {
            log.error("일일 배치 예측 업데이트 중 오류 발생", e);
        }
    }
}
