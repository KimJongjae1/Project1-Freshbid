import { useLocation, useParams } from "react-router-dom";
import { useState, useEffect } from "react";
import axiosInstance from "../../../api/axiosInstance";

import Review from "../../../components/seller/Review";
import Inquiry from "../../../components/seller/Inquiry";
import LiveList from "../../../components/seller/LiveList";
import { useUserStore } from "../../../stores/useUserStore";
import ProductList from "../../../components/seller/ProductList";

// 탭 타입 정의
type TabType = "경매" | "농장 리뷰" | "문의" | "라이브" | "상품";

// 판매자 정보 타입
interface SellerInfo {
	id: number;
	username: string;
	nickname: string;
	averageRating: number;
	totalReviews: number;
	profileImage: string | null;
	introduction?: string;
	phoneNumber: string;
	address: string;
	bookmarkCount: number;
}

const SellerDetail = () => {
	const location = useLocation();
	const { id } = useParams();
	const { farmName, tab } = location.state || {};

	const currentUsername = useUserStore((state) => state.username);
	const role = useUserStore((state) => state.role);
	const [activeTab, setActiveTab] = useState<TabType>(tab || "라이브");
	const [reviewCount, setReviewCount] = useState(0);
	const [sellerInfo, setSellerInfo] = useState<SellerInfo | null>(null);
	const [averageRating, setAverageRating] = useState(0);
	const [isBookmarked, setIsBookmarked] = useState<boolean>(false); //판매자 찜여부
	useEffect(() => {
		window.scrollTo(0, 0);
	}, []);

	useEffect(() => {
		if (tab) {
			setActiveTab(tab);
		}
	}, [tab]);

	// 판매자 정보 및 평점 조회
	const fetchSellerInfo = async () => {
		if (!id) return;

		try {
			// 판매자 기본 정보 조회
			const sellerResponse = await axiosInstance.get(`/seller-info/${id}`);
			const sellerData = sellerResponse.data.data;

			// 리뷰 목록 조회하여 평점 계산
			const reviewResponse = await axiosInstance.get(
				`/auction/review/seller/${id}?page=0&size=1000&sortBy=createdAt&sortDirection=DESC`
			);
			const reviews = reviewResponse.data.data.content || [];

			// 평점 평균 계산 (댓글 제외, 삭제된 리뷰 제외 - superId가 없고 삭제되지 않은 리뷰만)
			const mainReviews = reviews.filter((review: any) => !review.superId && !review.deleted);
			const totalRating = mainReviews.reduce((sum: number, review: any) => sum + review.rate, 0);
			const avgRating = mainReviews.length > 0 ? (totalRating / mainReviews.length).toFixed(1) : 0;

			//찜 여부 조회
			const response = await axiosInstance.get("/bookmark/seller");
			setIsBookmarked(response.data.data.some((item: { id: number }) => item.id === Number(id)));

			setSellerInfo({
				id: parseInt(id),
				username: sellerData?.username,
				nickname: sellerData?.nickname || farmName || "이름 없는 농장",
				averageRating: parseFloat(avgRating.toString()),
				totalReviews: mainReviews.length, // 댓글 제외, 삭제된 리뷰 제외한 리뷰 개수
				profileImage: sellerData?.profileImage,
				introduction: sellerData?.introduction,
				phoneNumber: sellerData?.phoneNumber,
				address: sellerData?.address,
				bookmarkCount: sellerData?.bookmarkCount,
			});

			setAverageRating(parseFloat(avgRating.toString()));
		} catch (err: any) {
			console.error("판매자 정보 조회 실패:", err);
			// 에러 시에도 기본값 설정
			setSellerInfo({
				id: parseInt(id || "1"),
				username: "",
				nickname: farmName || "이름 없는 농장",
				averageRating: 0,
				totalReviews: 0,
				introduction: "등록된 소개글이 없습니다.",
				phoneNumber: "전화번호 없음",
				address: "주소 없음",
				profileImage: null,
				bookmarkCount: 0,
			});
		}
	};

	//찜버튼 클릭
	const handleBookmarkClick = async () => {
		try {
			if (!isBookmarked) {
				await axiosInstance.post(`/bookmark/seller/${id}`);
				setIsBookmarked(true);
			} else {
				await axiosInstance.delete(`/bookmark/seller/${id}`);
				setIsBookmarked(false);
			}
		} catch (error) {
			console.error(error);
		}
	};

	// 컴포넌트 마운트 시 판매자 정보 조회
	useEffect(() => {
		fetchSellerInfo();
	}, [id]);

	// 리뷰 개수 변경 시 평점 재계산
	useEffect(() => {
		if (reviewCount > 0) {
			fetchSellerInfo();
		}
	}, [reviewCount]);

	// 탭 배열을 동적으로 생성
	const TABS: { key: TabType; label: string }[] = [
		{ key: "라이브", label: "라이브" },
		{ key: "상품", label: "상품" },
		{ key: "농장 리뷰", label: `농장 리뷰` },
		{ key: "문의", label: "문의" },
	];

	return (
		<div className="max-w-4xl mx-auto px-4 py-6">
			{/* 농장 상단 이미지 */}
			<div className="w-full h-40 bg-gray-100 overflow-hidden rounded-lg mb-6">
				<img src="/default.jpg" alt="농장 배너" className="w-full h-full object-cover" />
			</div>

			{/* 농장 프로필 */}
			<div className="flex items-center justify-between mb-6">
				<div className="flex items-center space-x-4">
					<img
						src={
							sellerInfo?.profileImage ? `data:image/jpeg;base64,${sellerInfo?.profileImage}` : "/farmer-profile.png"
						}
						alt="농장주"
						className="w-16 h-16 rounded-full object-cover"
					/>
					<div>
						<h2 className="text-xl font-bold text-gray-800">{sellerInfo?.nickname || farmName || "이름 없는 농장"}</h2>
						<p className="text-sm text-gray-500">{sellerInfo?.introduction || "등록된 소개글이 없습니다."}</p>
						{/* 추가 정보 */}
						{/* <div className="mt-2 text-sm text-gray-600 space-y-1">
              <p>📞 {sellerInfo?.phoneNumber || "전화번호 없음"}</p>
              <p>📍 {sellerInfo?.address || "주소 정보 없음"}</p>
            </div> */}
					</div>
					{role === "ROLE_CUSTOMER" && (
						<button
							onClick={handleBookmarkClick}
							className={`p-2 rounded-full transition-all duration-200 hover:scale-110 ${
								isBookmarked
									? "bg-red-500 text-white hover:bg-red-600"
									: "bg-white/80 text-gray-600 hover:bg-white hover:text-red-500"
							}`}
						>
							{isBookmarked ? (
								<svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
									<path d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" />
								</svg>
							) : (
								<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										strokeLinecap="round"
										strokeLinejoin="round"
										strokeWidth={2}
										d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
									/>
								</svg>
							)}
						</button>
					)}
				</div>

				<div className="flex items-center space-x-4">
					<div className="text-yellow-500 text-lg font-semibold">
						★ {averageRating > 0 ? averageRating.toFixed(1) : "0.0"}
					</div>
					<div className="text-red-500 text-lg">♥ {sellerInfo?.bookmarkCount || 0}</div>
				</div>
			</div>

			{/* 탭 바 */}
			<div className="border-b mb-6 flex justify-center">
				<ul className="flex space-x-6 text-gray-600 font-medium">
					{TABS.map((tabItem) => (
						<li
							key={tabItem.key}
							onClick={() => setActiveTab(tabItem.key)}
							className={`pb-2 cursor-pointer ${
								activeTab === tabItem.key ? "border-b-2 border-green-500 text-green-500" : "hover:text-green-500"
							}`}
						>
							{tabItem.label}
						</li>
					))}
				</ul>
			</div>

			{/* 탭 컨텐츠 */}
			<div className="min-h-32 text-gray-800 text-sm">
				{activeTab === "라이브" && (
					<div className="text-center">
						<LiveList id={id ?? ""} isOwner={currentUsername === sellerInfo?.username} />
					</div>
				)}
				{activeTab === "상품" && (
					<ProductList id={id ? Number(id) : 0} isOwner={currentUsername === sellerInfo?.username} />
				)}
				{activeTab === "농장 리뷰" && (
					<div className="text-center text-gray-400">
						<Review onReviewCountChange={setReviewCount} />
					</div>
				)}

				{activeTab === "문의" && <Inquiry isOwner={currentUsername === sellerInfo?.username} />}
			</div>
		</div>
	);
};

export default SellerDetail;
