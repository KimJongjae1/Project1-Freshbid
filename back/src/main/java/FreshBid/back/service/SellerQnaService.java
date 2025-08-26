package FreshBid.back.service;

import FreshBid.back.dto.SellerQna.*;
import FreshBid.back.entity.User;
import org.springframework.data.domain.Page;

public interface SellerQnaService {

    /**
     * 문의 등록
     */
    void createQna(SellerQnaCreateRequestDto dto, User user);

    /**
     * 문의 수정
     */
    void updateQna(Long qnaId, SellerQnaUpdateRequestDto dto, Long userId);

    /**
     * 문의 삭제
     */
    void deleteQna(Long qnaId, Long userId);

    /**
     * 판매자 문의 목록 조회 (정렬/필터 포함)
     */
    Page<SellerQnaResponseDto> getQnasBySeller(SellerQnaSearchRequestDto dto);

    /**
     * 문의 단건 조회
     */
    SellerQnaResponseDto getQnaById(Long qnaId);
} 