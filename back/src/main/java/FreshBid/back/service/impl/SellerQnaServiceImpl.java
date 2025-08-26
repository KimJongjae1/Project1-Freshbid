package FreshBid.back.service.impl;

import FreshBid.back.dto.SellerQna.*;
import FreshBid.back.entity.SellerQna;
import FreshBid.back.entity.User;
import FreshBid.back.exception.ForbiddenException;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.SellerQnaRepository;
import FreshBid.back.repository.SellerQnaRepositorySupport;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.service.SellerQnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerQnaServiceImpl implements SellerQnaService {

    private final SellerQnaRepository qnaRepository;
    private final SellerQnaRepositorySupport qnaRepositorySupport;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createQna(SellerQnaCreateRequestDto dto, User user) {
        log.info("[판매자 문의 등록 요청] 유저ID: {}, DTO: {}", user.getId(), dto);

        // 1. 판매자 존재 여부 확인
        User seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 판매자입니다: " + dto.getSellerId()));

        // 2. 부모 문의 검증 (superId가 있는 경우 - 답변)
        SellerQna superQna = null;
        if (dto.getSuperId() != null) {
            superQna = qnaRepository.findByIdAndIsDeletedFalse(dto.getSuperId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 부모 문의입니다: " + dto.getSuperId()));

            // 부모 문의 작성자 정보 null 체크
            if (superQna.getWriter() == null) {
                log.warn("부모 문의 작성자 정보가 없습니다. 부모 문의ID: {}", dto.getSuperId());
                throw new ForbiddenException("부모 문의 작성자 정보를 확인할 수 없습니다.");
            }

            // 부모 문의의 부모가 있는지 확인 (답변의 답변 방지)
            if (superQna.getSuperQna() != null) {
                log.warn("답변에는 답변을 작성할 수 없습니다. 부모 문의ID: {}", dto.getSuperId());
                throw new ForbiddenException("답변에는 답변을 작성할 수 없습니다.");
            }

            // 부모 문의가 같은 판매자에 대한 것인지 확인
            if (!superQna.getSeller().getId().equals(dto.getSellerId())) {
                log.warn("부모 문의와 다른 판매자에 대한 답변입니다. 부모 문의 판매자ID: {}, 요청 판매자ID: {}", 
                        superQna.getSeller().getId(), dto.getSellerId());
                throw new ForbiddenException("부모 문의와 같은 판매자에 대한 답변만 작성할 수 있습니다.");
            }

            // 답변 작성 권한 확인 (판매자만 답변 가능)
            if (!seller.getId().equals(user.getId())) {
                log.warn("판매자만 답변을 작성할 수 있습니다. 요청자ID: {}, 판매자ID: {}", user.getId(), seller.getId());
                throw new ForbiddenException("판매자만 답변을 작성할 수 있습니다.");
            }
        }

        // 3. 문의 생성 및 저장
        SellerQna qna = SellerQna.from(dto, user, seller, superQna);
        qnaRepository.save(qna);

        log.info("[판매자 문의 등록 완료] 문의ID: {}, 부모 문의ID: {}", qna.getId(), dto.getSuperId());
    }

    @Override
    @Transactional
    public void updateQna(Long qnaId, SellerQnaUpdateRequestDto dto, Long userId) {
        log.info("[판매자 문의 수정 요청] 문의ID: {}, 요청자ID: {}", qnaId, userId);

        SellerQna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 문의입니다: " + qnaId));

        // 작성자 정보 null 체크
        if (qna.getWriter() == null) {
            log.warn("문의 작성자 정보가 없습니다. 문의ID: {}, 요청자ID: {}", qnaId, userId);
            throw new ForbiddenException("문의 작성자 정보를 확인할 수 없습니다.");
        }

        if (!qna.getWriter().getId().equals(userId)) {
            log.warn("문의 수정 권한 없음 - 요청자ID: {}, 작성자ID: {}", userId, qna.getWriter().getId());
            throw new ForbiddenException("문의 수정 권한이 없습니다.");
        }

        qna.update(dto);
        qnaRepository.save(qna);
        log.info("[판매자 문의 수정 완료] 문의ID: {}", qnaId);
    }

    @Override
    @Transactional
    public void deleteQna(Long qnaId, Long userId) {
        log.info("[판매자 문의 삭제 요청] 문의ID: {}, 요청자ID: {}", qnaId, userId);

        SellerQna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 문의입니다: " + qnaId));

        // 작성자 정보 null 체크
        if (qna.getWriter() == null) {
            log.warn("문의 작성자 정보가 없습니다. 문의ID: {}, 요청자ID: {}", qnaId, userId);
            throw new ForbiddenException("문의 작성자 정보를 확인할 수 없습니다.");
        }

        if (!qna.getWriter().getId().equals(userId)) {
            log.warn("문의 삭제 권한 없음 - 요청자ID: {}, 작성자ID: {}", userId, qna.getWriter().getId());
            throw new ForbiddenException("문의 삭제 권한이 없습니다.");
        }

        // Soft Delete 처리 - 답변들은 그대로 유지
        qna.setDeleted(true);
        qnaRepository.save(qna);
        
        log.info("[판매자 문의 삭제 완료] 문의ID: {}, Soft Delete 처리됨 (답변들은 유지)", qnaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SellerQnaResponseDto> getQnasBySeller(SellerQnaSearchRequestDto req) {
        log.info("[판매자 문의 목록 조회] 조건: {}", req);

        Page<SellerQna> page = qnaRepositorySupport.search(req);
        return page.map(SellerQnaResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerQnaResponseDto getQnaById(Long qnaId) {
        log.info("[판매자 문의 단건 조회] 문의ID: {}", qnaId);

        SellerQna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 문의입니다: " + qnaId));

        return SellerQnaResponseDto.from(qna);
    }
} 