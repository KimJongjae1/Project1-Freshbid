import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import ReviewCard from "./ReviewCard";
import ReviewForm from "./ReviewForm";

// 백엔드 API 응답 타입 정의
interface SellerReviewResponseDto {
	id: number;
	userId: number;
	nickname: string;
	sellerId: number;
	orderId: number;
	content: string;
	rate: number;
	reviewImage?: string;
	superId?: number;
	createdAt: string;
	updatedAt: string;
	deleted: boolean;
}

interface ReviewCreateRequestDto {
	orderId: number;
	sellerId: number;
	superId?: number;
	content: string;
	rate: number;
	reviewImage?: string;
}

// interface ReviewSearchRequestDto {
// 	sellerId: number;
// 	page: number;
// 	size: number;
// 	sortBy: string;
// 	sortDirection: string;
// }

interface PageResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	size: number;
	number: number;
	first: boolean;
	last: boolean;
}

type ReviewProps = {
	onReviewCountChange?: (count: number) => void;
};

const Review = ({ onReviewCountChange }: ReviewProps) => {
	const { id } = useParams<{ id: string }>();
	const [reviews, setReviews] = useState<SellerReviewResponseDto[]>([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState<string | null>(null);
	const [page, setPage] = useState(0);
	const [hasMore, setHasMore] = useState(true);
	const [totalReviews, setTotalReviews] = useState(0);

	// 리뷰 목록 조회
	const fetchReviews = async (pageNum: number = 0, append: boolean = false) => {
		if (!id) return;

		try {
			setLoading(true);
			setError(null);

			// 백엔드 API에 맞춰서 쿼리 파라미터 구성
			const searchParams = new URLSearchParams({
				page: pageNum.toString(),
				size: "10",
				sortBy: "createdAt",
				sortDirection: "DESC",
			});

			// 올바른 API 엔드포인트 호출
			const response = await axiosInstance.get(`/auction/review/seller/${id}?${searchParams}`);
			const data = response.data.data as PageResponse<SellerReviewResponseDto>;

			if (append) {
				setReviews((prev) => [...prev, ...data.content]);
			} else {
				setReviews(data.content);
			}

			setTotalReviews(data.totalElements);
			setHasMore(!data.last);
			setPage(data.number);

			// 부모 컴포넌트에 리뷰 개수 전달
			onReviewCountChange?.(data.totalElements);
		} catch (err: any) {
			console.error("리뷰 조회 실패:", err);
			setError(err.response?.data?.message || "리뷰를 불러오는데 실패했습니다.");
			// 에러 시에도 0으로 설정
			onReviewCountChange?.(0);
		} finally {
			setLoading(false);
		}
	};

	// 리뷰 작성
	const handleReviewSubmit = async (rating: number, content: string, orderId?: number) => {
		if (!id) {
			setError("판매자 정보를 찾을 수 없습니다.");
			return { success: false, error: "판매자 정보를 찾을 수 없습니다." };
		}

		try {
			// 주문 ID가 없으면 에러 처리 (실제로는 주문 선택이 필요)
			if (!orderId) {
				setError("리뷰를 작성하려면 주문을 선택해주세요.");
				return { success: false, error: "리뷰를 작성하려면 주문을 선택해주세요." };
			}

			const reviewData: ReviewCreateRequestDto = {
				orderId: orderId,
				sellerId: parseInt(id),
				content: content,
				rate: rating,
			};

			console.log("리뷰 작성 요청 데이터:", reviewData);
			const response = await axiosInstance.post("/auction/review", reviewData);
			console.log("리뷰 작성 응답:", response.data);

			// 리뷰 작성 후 목록 새로고침
			await fetchReviews(0, false);

			return { success: true };
		} catch (err: any) {
			console.error("리뷰 작성 실패:", err);
			console.error("에러 응답:", err.response?.data);
			const errorMessage = err.response?.data?.message || "리뷰 작성에 실패했습니다.";
			setError(errorMessage);
			return { success: false, error: errorMessage };
		}
	};

	// 리뷰 수정
	const handleReviewUpdate = async (reviewId: number, rating: number, content: string) => {
		try {
			await axiosInstance.put(`/auction/review/${reviewId}`, {
				content: content,
				rate: rating,
			});

			// 수정 후 목록 새로고침
			await fetchReviews(0, false);

			return { success: true };
		} catch (err: any) {
			console.error("리뷰 수정 실패:", err);
			const errorMessage = err.response?.data?.message || "리뷰 수정에 실패했습니다.";
			setError(errorMessage);
			return { success: false, error: errorMessage };
		}
	};

	// 리뷰 삭제
	const handleReviewDelete = async (reviewId: number) => {
		try {
			await axiosInstance.delete(`/auction/review/${reviewId}`);

			// 삭제 후 전체 리뷰 목록 새로고침 (댓글도 함께 처리)
			await fetchReviews(0, false);

			return { success: true };
		} catch (err: any) {
			console.error("리뷰 삭제 실패:", err);
			return {
				success: false,
				error: err.response?.data?.message || "리뷰 삭제에 실패했습니다.",
			};
		}
	};

	// 댓글 작성 핸들러
	const handleReviewReply = async (superId: number, content: string) => {
		if (!id) {
			return { success: false, error: "판매자 ID가 없습니다." };
		}

		try {
			// 부모 리뷰 찾기
			const parentReview = reviews.find((review) => review.id === superId);
			if (!parentReview) {
				return { success: false, error: "부모 리뷰를 찾을 수 없습니다." };
			}

			const requestData: ReviewCreateRequestDto = {
				orderId: parentReview.orderId, // 부모 리뷰의 orderId 사용
				sellerId: parseInt(id),
				superId: superId,
				content: content,
				rate: 0, // 댓글은 별점 없음
			};

			await axiosInstance.post("/auction/review", requestData);

			// 댓글 작성 후 리뷰 목록 새로고침
			await fetchReviews(0, false);

			return { success: true };
		} catch (err: any) {
			console.error("댓글 작성 실패:", err);
			return {
				success: false,
				error: err.response?.data?.message || "댓글 작성에 실패했습니다.",
			};
		}
	};

	// 더보기 버튼 클릭
	const handleLoadMore = () => {
		if (hasMore && !loading) {
			fetchReviews(page + 1, true);
		}
	};

	// 초기 로드
	useEffect(() => {
		fetchReviews();
	}, [id]);

	// 에러 메시지 자동 제거
	useEffect(() => {
		if (error) {
			const timer = setTimeout(() => setError(null), 5000);
			return () => clearTimeout(timer);
		}
	}, [error]);

	return (
		<div className="mt-8 space-y-6">
			{/* 에러 메시지 */}
			{error && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">{error}</div>}

			{/* 리뷰 작성 영역 */}
			<ReviewForm onSubmit={handleReviewSubmit} />

			{/* 리뷰 목록 */}
			<div className="space-y-4">
				{loading && reviews.length === 0 ? (
					<div className="text-center py-8">
						<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-500 mx-auto"></div>
						<p className="text-gray-500 mt-2">리뷰를 불러오는 중...</p>
					</div>
				) : reviews.length === 0 ? (
					<div className="text-center py-8">
						<p className="text-gray-500">첫 리뷰를 작성해보세요!</p>
					</div>
				) : (
					<>
						{/* 리뷰 개수 표시 */}
						<div className="text-sm text-gray-600 mb-4">총 {totalReviews}개의 리뷰</div>

						{/* 리뷰 카드들 */}
						{reviews
							.filter((review) => {
								// 최상위 리뷰만
								if (review.superId) return false;

								// 삭제된 리뷰인 경우, 댓글이 있는지 확인
								if (review.deleted) {
									const hasReplies = reviews.some((reply) => reply.superId === review.id && !reply.deleted);
									// 댓글이 있는 삭제된 리뷰만 표시
									return hasReplies;
								}

								// 삭제되지 않은 리뷰는 모두 표시
								return true;
							})
							.map((review) => {
								// 해당 리뷰의 댓글들 찾기 (삭제되지 않은 댓글만)
								const replies = reviews.filter((reply) => reply.superId === review.id && !reply.deleted);

								return (
									<div key={review.id} className="space-y-3">
										{/* 메인 리뷰 (삭제된 경우에도 표시) */}
										<ReviewCard
											review={review}
											onUpdate={handleReviewUpdate}
											onDelete={handleReviewDelete}
											onReply={handleReviewReply}
										/>

										{/* 댓글들 (삭제된 댓글은 제외) */}
										{replies.map((reply) => (
											<div key={reply.id} className="ml-8">
												<ReviewCard
													review={reply}
													onUpdate={handleReviewUpdate}
													onDelete={handleReviewDelete}
													onReply={handleReviewReply}
												/>
											</div>
										))}
									</div>
								);
							})}

						{/* 삭제된 부모 리뷰의 댓글들도 독립적으로 표시 (삭제된 댓글은 제외, 이미 위에서 표시된 댓글은 제외) */}
						{reviews
							.filter((review) => {
								// superId가 있고 (댓글인 경우)
								if (!review.superId) return false;

								// 삭제된 댓글은 제외
								if (review.deleted) return false;

								// 부모 리뷰를 찾기
								const parentReview = reviews.find((r) => r.id === review.superId);

								// 부모 리뷰가 삭제된 경우만 (고아 댓글)
								if (!parentReview || !parentReview.deleted) return false;

								// 이미 위에서 표시된 댓글인지 확인 (댓글이 있는 삭제된 리뷰는 위에서 이미 표시됨)
								const isAlreadyDisplayed = reviews
									.filter((r) => {
										// 최상위 리뷰들 중에서
										if (r.superId) return false;

										// 삭제된 리뷰인 경우, 댓글이 있는지 확인
										if (r.deleted) {
											const hasReplies = reviews.some((reply) => reply.superId === r.id && !reply.deleted);
											// 댓글이 있는 삭제된 리뷰만 확인
											return hasReplies;
										}

										// 삭제되지 않은 리뷰는 모두 확인
										return true;
									})
									.some((topReview) => {
										const topReviewReplies = reviews.filter(
											(reply) => reply.superId === topReview.id && !reply.deleted
										);
										return topReviewReplies.some((reply) => reply.id === review.id);
									});

								return !isAlreadyDisplayed;
							})
							.map((orphanedReply) => (
								<div key={orphanedReply.id} className="ml-8">
									<ReviewCard
										review={orphanedReply}
										onUpdate={handleReviewUpdate}
										onDelete={handleReviewDelete}
										onReply={handleReviewReply}
									/>
								</div>
							))}

						{/* 더보기 버튼 */}
						{hasMore && (
							<div className="flex justify-center mt-6">
								<button
									onClick={handleLoadMore}
									disabled={loading}
									className="px-6 py-2 bg-green-500 text-white rounded-full hover:bg-green-600 transition disabled:opacity-50"
								>
									{loading ? "로딩 중..." : "더보기"}
								</button>
							</div>
						)}
					</>
				)}
			</div>
		</div>
	);
};

export default Review;
