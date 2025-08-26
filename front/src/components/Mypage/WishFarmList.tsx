// components/Mypage/BookmarkedSellers.tsx

import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import { useUserStore } from "../../stores/useUserStore";

interface Seller {
	id: number;
	name: string;
	profileImage: string;
	introduction: string;
}

const BookmarkedSellers = () => {
	const [bookmarkedSellers, setBookmarkedSellers] = useState<Seller[]>([]);
	const [loading, setLoading] = useState(true);
	const [isLoading, setIsLoading] = useState(false);
	const navigate = useNavigate();
	const { isLoggedIn } = useUserStore();

	// API에서 찜한 판매자 목록 가져오기
	const fetchBookmarkedSellers = async () => {
		try {
			setLoading(true);
			const response = await axiosInstance.get("bookmark/seller");
			const datas = response.data.data;
			console.log(datas);

			// 최신 북마크순으로 정렬 (id 기준 내림차순)
			const sortedData = datas.sort((a: any, b: any) => b.id - a.id);
			const mappedData = sortedData.map((seller: any) => {
				return {
					id: seller.id,
					name: seller.nickname,
					profileImage: "/default-profile.png", // public 폴더의 이미지 사용
					introduction: seller.introduction,
				};
			});
			setBookmarkedSellers(mappedData);
		} catch (err) {
			console.error("찜한 판매자 목록 조회 실패:", err);
		} finally {
			setLoading(false);
		}
	};

	// 판매자 북마크 토글 (찜/해제)
	const handleBookmarkToggle = async (sellerId: number) => {
		if (!isLoggedIn) {
			alert("로그인이 필요한 서비스입니다.");
			return;
		}

		if (isLoading) return;

		setIsLoading(true);
		try {
			await axiosInstance.delete(`bookmark/seller/${sellerId}`);
			setBookmarkedSellers(bookmarkedSellers.filter((seller) => seller.id !== sellerId));
		} catch (err) {
			console.error("북마크 해제 실패:", err);
			alert("북마크 해제에 실패했습니다.");
		} finally {
			setIsLoading(false);
		}
	};

	useEffect(() => {
		fetchBookmarkedSellers();
	}, []);

	if (loading) {
		return (
			<div className="flex justify-center items-center p-8">
				<div className="text-gray-500">찜한 판매자를 불러오는 중...</div>
			</div>
		);
	}

	if (bookmarkedSellers.length === 0) {
		return (
			<div>
				<h2 className="text-xl font-semibold mb-4">찜한 판매자</h2>
				<div className="bg-white border border-gray-200 rounded-lg shadow-sm p-8 text-center">
					<div className="text-gray-500">찜한 판매자가 없습니다.</div>
				</div>
			</div>
		);
	}

	return (
		<div>
			<h2 className="text-xl font-semibold mb-4">찜한 판매자</h2>

			<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
				{bookmarkedSellers.map((seller) => (
					<div onClick={() => navigate(`/seller/detail/${seller.id}`)}
						key={seller.id}
						className="bg-white border border-gray-200 rounded-lg shadow-sm p-4 hover:shadow-md transition-shadow flex items-center"
					>
						<div className="flex items-center space-x-3 w-full">
							{/* 판매자 프로필 이미지 */}
							<div className="flex-shrink-0">
								<img
									src={seller.profileImage}
									alt={`${seller.name} 프로필`}
									className="w-12 h-12 rounded-full object-cover border-2 border-gray-200"
								/>
							</div>

							{/* 판매자 정보 */}
							<div className="flex-1 min-w-0">
								<div className="flex items-center space-x-2">
									<button
										onClick={() => navigate(`/seller/detail/${seller.id}`)}
										className="font-medium text-gray-900 hover:text-green-600 transition-colors cursor-pointer text-left"
									>
										{seller.name}
									</button>
								</div>
								<div className="text-sm text-gray-500 mt-1 line-clamp-2">{seller.introduction}</div>
							</div>

							{/* 하트 아이콘 (찜 토글) */}
							<div className="flex-shrink-0">
								<button
									onClick={(e) => {
										e.stopPropagation();
										handleBookmarkToggle(seller.id);
									}}
									disabled={isLoading}
									className={`p-2 rounded-full transition-all duration-200 ${
										"bg-red-500 text-white hover:bg-red-600"
									} ${isLoading ? "opacity-50 cursor-not-allowed" : "hover:scale-110"}`}
								>
									<svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
										<path d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" />
									</svg>
								</button>
							</div>
						</div>
					</div>
				))}
			</div>
		</div>
	);
};

export default BookmarkedSellers;
