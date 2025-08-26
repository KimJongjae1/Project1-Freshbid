import { useEffect, useState } from "react";
import axiosInstance from "../../../api/axiosInstance";
import LiveCard from "../../../components/Auction/LiveCard";
import SearchBar from "../../../components/Auction/SearchBar";
import Pagination from "../../../components/pagination";
import { usePagination } from "../../../hooks/usePagination";

type AuctionStatus = "scheduled" | "active" | "closed";

type AuctionInLive = {
	auctionId: number;
	startPrice: number;
	currentPrice: number;
	likeCount: number;
	startTime: string;
	endTime: string;
	status?: AuctionStatus;
	product: {
		title: string;
		imageUrl: string;
		category: string;
		deliveryDate: string;
	};
	farm: {
		id: number;
		name: string;
	};
};
type LiveStatus = "SCHEDULED" | "IN_PROGRESS" | "ENDED";

type LiveListItem = {
	id: number;
	title: string;
	startDate: string;
	endDate: string;
	reprImgSrc: string | null;
	liveStatus: LiveStatus;
	seller: {
		sellerId: number;
		nickname: string;
	};
	auctions: AuctionInLive[];
};

type ApiResponse<T> = {
	success: boolean;
	message: string;
	data: {
		content: T[];
		totalElements: number;
		totalPages: number;
		size: number;
		number: number;
		first: boolean;
		last: boolean;
		empty: boolean;
	};
};

export default function LiveAuctionView() {
	const [liveList, setLiveList] = useState<LiveListItem[]>([]);
	const [searchQuery, setSearchQuery] = useState<string>("");
	const [searchType, setSearchType] = useState<"all" | "title" | "product" | "seller">("all");
	const [totalElements, setTotalElements] = useState(0);
	const [loading, setLoading] = useState(false);

	// 페이지네이션 훅
	const pagination = usePagination(totalElements, {
		initialPage: 1,
		pageSize: 9,
		blockSize: 10,
		enableUrlSync: true,
	});

	// 페이지 이동 시 스크롤을 맨 위로 이동
	useEffect(() => {
		window.scrollTo(0, 0);
	}, []);

	// 페이지 변경 시 스크롤을 맨 위로 이동
	useEffect(() => {
		window.scrollTo({ top: 0, behavior: "smooth" });
	}, [pagination.currentPage]);

	useEffect(() => {
		const fetchLives = async () => {
			setLoading(true);
			try {
				const params: Record<string, string | number | string[]> = {
					page: pagination.currentPage - 1, // Spring Boot는 0부터 시작
					size: pagination.pageSize,
					sortBy: "endDate",
					sortDirection: "ASC",
					endDateFrom: new Date().toISOString().replace("Z", "").split(".")[0], // 현재 시간 이후
					statuses: ["SCHEDULED", "IN_PROGRESS"], // ENDED 상태 제외
				};

				// 검색 기준에 따른 파라미터 추가
				if (searchQuery.trim()) {
					switch (searchType) {
						case "title":
							params.title = searchQuery.trim();
							break;
						case "product":
							params.productName = searchQuery.trim();
							break;
						case "seller":
							params.sellerNickname = searchQuery.trim();
							break;
						case "all":
						default:
							// OR 조건 통합 검색
							params.searchQuery = searchQuery.trim();
							break;
					}
				}

				const response = await axiosInstance.get<ApiResponse<LiveListItem>>("/auction/live", {
					params,
					paramsSerializer: function (params) {
						return Object.keys(params)
							.map((key) => {
								const value = params[key];
								if (Array.isArray(value)) {
									return value.map((v) => `${key}=${encodeURIComponent(v)}`).join("&");
								}
								return `${key}=${encodeURIComponent(value)}`;
							})
							.join("&");
					},
				});
				console.log(response);

				// 서버에서 이미 필터링된 데이터를 그대로 사용
				setLiveList(response.data.data.content);
				setTotalElements(response.data.data.totalElements);
			} catch (err) {
				console.error("❌ 라이브 불러오기 실패:", err);
				setLiveList([]);
				setTotalElements(0);
			} finally {
				setLoading(false);
			}
		};

		fetchLives();
	}, [pagination.currentPage, pagination.pageSize, searchQuery, searchType]);

	// 서버에서 이미 필터링된 데이터를 그대로 사용
	const visibleLives = liveList;

	return (
		<div className="p-6 md:p-8 max-w-6xl mx-auto">
			<h1 className="text-2xl font-bold text-gray-800 text-center mt-5">실시간 경매 목록</h1>
			<p className="text-gray-800 text-center my-2">진행 중인 라이브 방송을 확인해보세요!</p>

			<div className="w-full max-w-4xl mx-auto ">
				<div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center bg-white p-4 rounded-lg  ">
					{/* 검색 영역 */}
					<div className="flex flex-1 gap-2 items-center min-w-0">
						{/* 검색 기준 선택 */}
						<select
							value={searchType}
							onChange={(e) => setSearchType(e.target.value as "all" | "title" | "product" | "seller")}
							className="w-[120px] text-center flex-shrink-0  py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-sm"
						>
							<option value="all">전체</option>
							<option value="title">라이브 제목</option>
							<option value="product">상품명</option>
							<option value="seller">판매자명</option>
						</select>

						{/* 검색바 */}
						<div className="flex-1 min-w-0">
							<SearchBar onSearch={setSearchQuery} />
						</div>
						<div className="w-[120px] "></div>
					</div>
				</div>
			</div>

			{/* 로딩 상태 */}
			{loading && (
				<div className="flex justify-center mt-6">
					<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
				</div>
			)}

			<div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 justify-center px-2 mt-10">
				{visibleLives.length === 0 ? (
					<p className="col-span-full text-center text-gray-400 mt-4">
						{loading ? "로딩 중..." : "진행 중인 라이브가 없습니다."}
					</p>
				) : (
					visibleLives.map((live) => <LiveCard key={live.id} live={live} />)
				)}
			</div>

			{/* 페이지네이션 */}
			{pagination.totalPages > 1 && (
				<div className="flex justify-center mt-8">
					<Pagination
						currentPage={pagination.currentPage}
						totalPages={pagination.totalPages}
						onPageChange={pagination.goToPage}
						blockSize={10}
						goToPrevBlock={pagination.goToPrevBlock}
						goToNextBlock={pagination.goToNextBlock}
						hasPrevBlock={pagination.hasPrevBlock}
						hasNextBlock={pagination.hasNextBlock}
						getVisiblePages={pagination.getVisiblePages}
						disabled={loading}
						className="mt-4"
					/>
				</div>
			)}
		</div>
	);
}
