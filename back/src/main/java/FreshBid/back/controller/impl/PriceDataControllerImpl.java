package FreshBid.back.controller.impl;

import FreshBid.back.controller.PriceDataController;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.price.PriceChartResponseDto;
import FreshBid.back.dto.price.PriceDataRequestDto;
import FreshBid.back.service.PriceDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceDataControllerImpl implements PriceDataController {

    private final PriceDataService priceDataService;

    @Override
    public ResponseEntity<CommonResponse<PriceChartResponseDto>> getPriceChartData(Long categoryId, String grade) {
        log.info("가격 차트 데이터 조회 요청 - 카테고리ID: {}, 등급: {}", categoryId, grade);

        PriceChartResponseDto chartData = priceDataService.getPriceChartData(categoryId, grade);

        CommonResponse<PriceChartResponseDto> response = CommonResponse.<PriceChartResponseDto>builder()
                .success(true)
                .message("가격 차트 데이터 조회에 성공했습니다.")
                .data(chartData)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getAvailableCategories() {
        log.info("사용 가능한 카테고리 목록 조회 요청");

        List<PriceDataRequestDto> categories = priceDataService.getAvailableCategories();

        CommonResponse<List<PriceDataRequestDto>> response = CommonResponse.<List<PriceDataRequestDto>>builder()
                .success(true)
                .message("카테고리 목록 조회에 성공했습니다.")
                .data(categories)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getSuperCategories() {
        log.info("상위 카테고리 목록 조회 요청");

        List<PriceDataRequestDto> categories = priceDataService.getSuperCategories();

        CommonResponse<List<PriceDataRequestDto>> response = CommonResponse.<List<PriceDataRequestDto>>builder()
                .success(true)
                .message("상위 카테고리 목록 조회에 성공했습니다.")
                .data(categories)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getSubCategories(Long superCategoryId) {
        log.info("하위 카테고리 목록 조회 요청 - 상위 카테고리ID: {}", superCategoryId);

        List<PriceDataRequestDto> categories = priceDataService.getSubCategories(superCategoryId);

        CommonResponse<List<PriceDataRequestDto>> response = CommonResponse.<List<PriceDataRequestDto>>builder()
                .success(true)
                .message("하위 카테고리 목록 조회에 성공했습니다.")
                .data(categories)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public CommonResponse<List<String>> getAvailableGrades(Long categoryId) {
        try {
            List<String> grades = priceDataService.getAvailableGrades(categoryId);
            return CommonResponse.<List<String>>builder()
                    .success(true)
                    .message("사용 가능한 등급 목록 조회에 성공했습니다.")
                    .data(grades)
                    .build();
        } catch (Exception e) {
            log.error("사용 가능한 등급 조회 중 오류 발생", e);
            return CommonResponse.<List<String>>builder()
                    .success(false)
                    .message("사용 가능한 등급 조회에 실패했습니다.")
                    .data(null)
                    .build();
        }
    }

    @Override
    public CommonResponse<List<PriceDataRequestDto>> getAvailableCategoriesWithData() {
        try {
            List<PriceDataRequestDto> categories = priceDataService.getAvailableCategoriesWithData();
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(true)
                    .message("데이터가 있는 카테고리 목록 조회에 성공했습니다.")
                    .data(categories)
                    .build();
        } catch (Exception e) {
            log.error("데이터가 있는 카테고리 조회 중 오류 발생", e);
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(false)
                    .message("카테고리 조회에 실패했습니다.")
                    .data(null)
                    .build();
        }
    }

    @Override
    public CommonResponse<List<PriceDataRequestDto>> getSuperCategoriesWithData() {
        try {
            List<PriceDataRequestDto> categories = priceDataService.getSuperCategoriesWithData();
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(true)
                    .message("데이터가 있는 상위 카테고리 목록 조회에 성공했습니다.")
                    .data(categories)
                    .build();
        } catch (Exception e) {
            log.error("데이터가 있는 상위 카테고리 조회 중 오류 발생", e);
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(false)
                    .message("상위 카테고리 조회에 실패했습니다.")
                    .data(null)
                    .build();
        }
    }

    @Override
    public CommonResponse<List<PriceDataRequestDto>> getSubCategoriesWithData(Long superCategoryId) {
        try {
            List<PriceDataRequestDto> categories = priceDataService.getSubCategoriesWithData(superCategoryId);
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(true)
                    .message("데이터가 있는 하위 카테고리 목록 조회에 성공했습니다.")
                    .data(categories)
                    .build();
        } catch (Exception e) {
            log.error("데이터가 있는 하위 카테고리 조회 중 오류 발생", e);
            return CommonResponse.<List<PriceDataRequestDto>>builder()
                    .success(false)
                    .message("하위 카테고리 조회에 실패했습니다.")
                    .data(null)
                    .build();
        }
    }
}
