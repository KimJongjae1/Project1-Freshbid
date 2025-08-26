package FreshBid.back.controller;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.price.PriceChartResponseDto;
import FreshBid.back.dto.price.PriceDataRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "가격 데이터 API", description = "가격 차트 및 예측 데이터 관련 API")
@RequestMapping("/price")
public interface PriceDataController {

    @Operation(summary = "가격 차트 데이터 조회", description = "특정 카테고리와 등급의 가격 차트 데이터를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가격 차트 데이터 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/chart/{categoryId}/{grade}")
    ResponseEntity<CommonResponse<PriceChartResponseDto>> getPriceChartData(
        @Parameter(description = "카테고리 ID", example = "1")
        @PathVariable("categoryId") Long categoryId,
        @Parameter(description = "등급", example = "상")
        @PathVariable("grade") String grade
    );

    @Operation(summary = "사용 가능한 카테고리 목록 조회", description = "가격 데이터가 있는 카테고리 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/categories")
    ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getAvailableCategories();

    @Operation(summary = "상위 카테고리 목록 조회", description = "상위 카테고리 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상위 카테고리 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/super-categories")
    ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getSuperCategories();

    @Operation(summary = "하위 카테고리 목록 조회", description = "특정 상위 카테고리의 하위 카테고리 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "하위 카테고리 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/sub-categories/{superCategoryId}")
    ResponseEntity<CommonResponse<List<PriceDataRequestDto>>> getSubCategories(
        @Parameter(description = "상위 카테고리 ID", example = "1")
        @PathVariable("superCategoryId") Long superCategoryId
    );

    @Operation(summary = "카테고리별 사용 가능한 등급 조회", description = "특정 카테고리에서 사용 가능한 등급 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등급 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/available-grades/{categoryId}")
    CommonResponse<List<String>> getAvailableGrades(@PathVariable Long categoryId);

    @GetMapping("/categories-with-data")
    CommonResponse<List<PriceDataRequestDto>> getAvailableCategoriesWithData();

    @GetMapping("/super-categories-with-data")
    CommonResponse<List<PriceDataRequestDto>> getSuperCategoriesWithData();

    @GetMapping("/sub-categories-with-data/{superCategoryId}")
    CommonResponse<List<PriceDataRequestDto>> getSubCategoriesWithData(@PathVariable Long superCategoryId);
}
