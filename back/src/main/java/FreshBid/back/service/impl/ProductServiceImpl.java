
package FreshBid.back.service.impl;

import FreshBid.back.dto.product.*;
import FreshBid.back.entity.Product;
import FreshBid.back.entity.ProductCategory;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.ProductCategoryRepository;
import FreshBid.back.repository.ProductRepository;
import FreshBid.back.repository.ProductRepositorySupport;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.ProductService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductRepositorySupport productRepositorySupport;
    private final ProductCategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private static final String MINIO_PREFIX = "product";
    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto dto, User seller) {
        log.info("상품 등록 요청 - 판매자 ID: {}, 요청 데이터: {}", seller.getId(), dto);

        // 판매자 권한 검증
        if (seller.getRole() != User.Role.ROLE_SELLER) {
            log.warn("상품 등록 권한 없음 - 사용자 ID: {}, 역할: {}", seller.getId(), seller.getRole());
            throw new ForbiddenException("판매자만 상품을 등록할 수 있습니다.");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("상품 이름은 필수입니다.");
        }
        if (dto.getWeight() == null) {
            throw new IllegalArgumentException("상품 무게는 필수입니다.");
        }
        if (dto.getOrigin() == null || dto.getOrigin().isBlank()) {
            throw new IllegalArgumentException("원산지는 필수입니다.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
        }

        ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다: " + dto.getCategoryId()));

        //상품 이미지 minio 저장
        String imgFilePath = null;
        if(dto.getReprImgSrc() != null) {
            imgFilePath = fileStorageService.uploadImage(MINIO_PREFIX, dto.getReprImgSrc());
        }
        Product product = Product.of(dto, imgFilePath, seller, category);
        productRepository.save(product);
        log.info("상품 등록 완료 - 상품 ID: {}", product.getId());
        return ProductResponseDto.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        log.info("상품 조회 요청 - ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 상품입니다: " + productId));
        return ProductResponseDto.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(ProductSearchRequestDto req) {
        log.info("상품 검색 요청 - {}", req);

        Pageable pageable = PageRequest.of(
                req.getPage(),
                req.getSize(),
                Sort.by(Sort.Direction.fromString(req.getSortDirection()), req.getSortBy())
        );

        Page<Product> page = productRepositorySupport.searchProductsByCondition(req, pageable);

        return page.map(ProductResponseDto::toDto);
    }


    @Override
    @Transactional
    public void updateProduct(Long sellerId, Long productId, ProductUpdateRequestDto dto) {
        log.info("상품 수정 요청 - 상품ID: {}, 판매자ID: {}, 요청: {}", productId, sellerId, dto);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 상품입니다: " + productId));
        if (!product.getUser().getId().equals(sellerId)) {
            log.warn("상품 수정 권한 없음 - 요청 판매자ID: {}, 실제 판매자ID: {}", sellerId, product.getUser().getId());
            throw new ForbiddenException("상품 수정 권한이 없습니다.");
        }
        Optional.ofNullable(dto.getName()).ifPresent(product::setName);
        Optional.ofNullable(dto.getOrigin()).ifPresent(product::setOrigin);
        Optional.ofNullable(dto.getWeight()).ifPresent(product::setWeight);
        Optional.ofNullable(dto.getReprImgSrc()).ifPresent(product::setReprImgSrc);
        Optional.ofNullable(dto.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(dto.getCategoryId()).ifPresent(catId -> {
            ProductCategory cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다: " + catId));
            product.setCategory(cat);
        });
        Optional.ofNullable(dto.getGrade())
                .ifPresent(g -> product.setGrade(Product.Grade.valueOf(g)));
        productRepository.save(product);
        log.info("상품 수정 완료 - 상품ID: {}", productId);
    }

    @Override
    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        log.info("상품 삭제 요청 - 상품ID: {}, 판매자ID: {}", productId, sellerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 상품입니다: " + productId));
        if (!product.getUser().getId().equals(sellerId)) {
            log.warn("상품 삭제 권한 없음 - 요청 판매자ID: {}, 실제 판매자ID: {}", sellerId, product.getUser().getId());
            throw new ForbiddenException("상품 삭제 권한이 없습니다.");
        }
        product.setDeleted(true);
        productRepository.save(product);
        log.info("상품 삭제 완료 - 상품ID: {}", productId);
    }
}
