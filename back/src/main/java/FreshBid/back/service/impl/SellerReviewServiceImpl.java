package FreshBid.back.service.impl;

import FreshBid.back.dto.SellerReview.*;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.SellerReview;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.OrderRepository;
import FreshBid.back.repository.SellerReviewRepository;
import FreshBid.back.repository.SellerReviewRepositorySupport;
import FreshBid.back.service.SellerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerReviewServiceImpl implements SellerReviewService {

    private final SellerReviewRepository reviewRepository;
    private final SellerReviewRepositorySupport reviewRepositorySupport;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void createReview(SellerReviewCreateRequestDto dto, User user) {
        log.info("[판매자 후기 등록 요청] 유저ID: {}, DTO: {}", user.getId(), dto);

        // 1. 주문 존재 여부 확인
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 주문입니다: " + dto.getOrderId()));

        log.info("[주문 정보] 주문ID: {}, 구매자ID: {}, 판매자ID: {}, 상태: {}", 
                order.getId(), order.getCustomer().getId(), order.getSeller().getId(), order.getStatus());

        // 2. 주문 상태 검증 - 완료된 주문만 리뷰 작성 가능
        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            log.warn("주문 상태가 완료가 아닙니다. 주문ID: {}, 상태: {}", dto.getOrderId(), order.getStatus());
            throw new ForbiddenException("완료된 주문에만 리뷰를 작성할 수 있습니다. 현재 주문 상태: " + order.getStatus());
        }

        // 3. 권한 검증 - 구매자 또는 해당 주문의 판매자만 리뷰 작성 가능
        boolean isCustomer = order.getCustomer().getId().equals(user.getId());
        boolean isSeller = order.getSeller().getId().equals(user.getId());
        
        log.info("[권한 검증] 요청자ID: {}, 구매자여부: {}, 판매자여부: {}", user.getId(), isCustomer, isSeller);
        
        if (!isCustomer && !isSeller) {
            log.warn("리뷰 작성 권한 없음 - 요청자ID: {}, 주문 구매자ID: {}, 주문 판매자ID: {}", 
                    user.getId(), order.getCustomer().getId(), order.getSeller().getId());
            throw new ForbiddenException("해당 주문의 구매자 또는 판매자만 리뷰를 작성할 수 있습니다.");
        }

        // 4. 중복 리뷰 검증 - 같은 사용자가 같은 주문에 대해 이미 리뷰가 존재하는지 확인
        if (dto.getSuperId() == null) { // 최상위 리뷰인 경우에만 중복 검증
            boolean existsReview = reviewRepository.existsByOrderIdAndUserIdAndIsDeletedFalse(dto.getOrderId(), user.getId());
            log.info("[중복 검증] 주문ID: {}, 유저ID: {}, 기존 리뷰 존재: {}", dto.getOrderId(), user.getId(), existsReview);
            
            if (existsReview) {
                log.warn("이미 해당 주문에 대한 리뷰가 존재합니다. 주문ID: {}, 유저ID: {}", dto.getOrderId(), user.getId());
                throw new ForbiddenException("이미 해당 주문에 대한 리뷰를 작성했습니다.");
            }
        }

        // 5. 부모 리뷰 검증 (superId가 있는 경우)
        SellerReview superReview = null;
        if (dto.getSuperId() != null) {
            superReview = reviewRepository.findByIdAndIsDeletedFalse(dto.getSuperId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 부모 리뷰입니다: " + dto.getSuperId()));

            // 부모 리뷰 작성자 정보 null 체크
            if (superReview.getUser() == null) {
                log.warn("부모 리뷰 작성자 정보가 없습니다. 부모 리뷰ID: {}", dto.getSuperId());
                throw new ForbiddenException("부모 리뷰 작성자 정보를 확인할 수 없습니다.");
            }

            // 부모 리뷰의 부모가 있는지 확인 (대댓글의 대댓글 방지)
            if (superReview.getSuperReview() != null) {
                log.warn("대댓글에는 댓글을 작성할 수 없습니다. 부모 리뷰ID: {}", dto.getSuperId());
                throw new ForbiddenException("대댓글에는 댓글을 작성할 수 없습니다.");
            }

            // 부모 리뷰가 같은 주문에 대한 것인지 확인
            if (!superReview.getOrder().getId().equals(dto.getOrderId())) {
                log.warn("부모 리뷰와 다른 주문에 대한 댓글입니다. 부모 리뷰 주문ID: {}, 요청 주문ID: {}", 
                        superReview.getOrder().getId(), dto.getOrderId());
                throw new ForbiddenException("부모 리뷰와 같은 주문에 대한 댓글만 작성할 수 있습니다.");
            }

            // 댓글 작성 권한 검증 - 부모 리뷰 작성자와 다른 사람만 댓글 가능
            if (superReview.getUser().getId().equals(user.getId())) {
                log.warn("자신의 리뷰에는 댓글을 작성할 수 없습니다. 부모 리뷰 작성자ID: {}, 요청자ID: {}", 
                        superReview.getUser().getId(), user.getId());
                throw new ForbiddenException("자신의 리뷰에는 댓글을 작성할 수 없습니다.");
            }
        }

        // 6. 리뷰 생성 및 저장
        SellerReview review = SellerReview.from(dto, user, order, superReview);
        reviewRepository.save(review);

        log.info("[판매자 후기 등록 완료] 후기ID: {}, 부모 리뷰ID: {}", review.getId(), dto.getSuperId());
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, SellerReviewUpdateRequestDto dto, Long userId) {
        log.info("[판매자 후기 수정 요청] 후기ID: {}, 요청자ID: {}", reviewId, userId);

        SellerReview review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 후기입니다: " + reviewId));

        // 작성자 정보 null 체크
        if (review.getUser() == null) {
            log.warn("후기 작성자 정보가 없습니다. 후기ID: {}, 요청자ID: {}", reviewId, userId);
            throw new ForbiddenException("후기 작성자 정보를 확인할 수 없습니다.");
        }

        if (!review.getUser().getId().equals(userId)) {
            log.warn("후기 수정 권한 없음 - 요청자ID: {}, 작성자ID: {}", userId, review.getUser().getId());
            throw new ForbiddenException("후기 수정 권한이 없습니다.");
        }

        review.update(dto);
        reviewRepository.save(review);
        log.info("[판매자 후기 수정 완료] 후기ID: {}", reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("[판매자 후기 삭제 요청] 후기ID: {}, 요청자ID: {}", reviewId, userId);

        SellerReview review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 후기입니다: " + reviewId));

        // 작성자 정보 null 체크
        if (review.getUser() == null) {
            log.warn("후기 작성자 정보가 없습니다. 후기ID: {}, 요청자ID: {}", reviewId, userId);
            throw new ForbiddenException("후기 작성자 정보를 확인할 수 없습니다.");
        }

        if (!review.getUser().getId().equals(userId)) {
            log.warn("후기 삭제 권한 없음 - 요청자ID: {}, 작성자ID: {}", userId, review.getUser().getId());
            throw new ForbiddenException("후기 삭제 권한이 없습니다.");
        }

        // Soft Delete 처리 (댓글은 그대로 유지)
        review.setDeleted(true);
        reviewRepository.save(review);
        
        log.info("[판매자 후기 삭제 완료] 후기ID: {}, Soft Delete 처리됨 (댓글들은 유지)", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SellerReviewResponseDto> getReviewsBySeller(SellerReviewSearchRequestDto req) {
        log.info("[판매자 후기 목록 조회] 조건: {}", req);

        Page<SellerReview> page = reviewRepositorySupport.search(req);
        return page.map(SellerReviewResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerReviewResponseDto getReviewById(Long reviewId) {
        log.info("[판매자 후기 단건 조회] 후기ID: {}", reviewId);

        SellerReview review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 후기입니다: " + reviewId));

        return SellerReviewResponseDto.from(review);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderId(Long orderId) {
        log.info("[주문 기반 후기 존재 여부 확인] 주문ID: {}", orderId);
        return reviewRepository.existsByOrderIdAndIsDeletedFalse(orderId);
    }
}
