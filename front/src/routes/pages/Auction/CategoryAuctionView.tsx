// src/routes/pages/Category/CategoryLiveView.tsx

import { useEffect, useMemo, useState } from "react";
import axiosInstance from "../../../api/axiosInstance";
import LiveCard from "../../../components/Auction/LiveCard";
import SearchBar from "../../../components/Auction/SearchBar";
import Pagination from "../../../components/pagination";
import { usePagination } from "../../../hooks/usePagination";
import { useSearchParams } from "react-router-dom";

type AuctionStatus = "scheduled" | "active" | "closed" | "failed";

type Auction = {
	auctionId: number;
	startPrice: number;
	currentPrice: number;
	likeCount: number;
	startTime: string;
	endTime: string;
	status: AuctionStatus | string;
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

type LiveListItem = {
	id: number;
	title: string;
	startDate: string;
	endDate: string;
	reprImgSrc: string | null;
	liveStatus?: string;
	seller: {
		sellerId: number;
		nickname: string;
	};
	auctions: Auction[];
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

// function getDateISOString(date: Date) {
//   return date.toISOString().split(".")[0];
// }

// type Category = {
//   id: number;
//   name: string;
// }

// export default function CategoryLiveView() {
//   const [lives, setLives] = useState<LiveListItem[]>([]);
//   const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
//   const [searchQuery, setSearchQuery] = useState("");
//   const [visibleCount, setVisibleCount] = useState(9);
//   const [searchParams] = useSearchParams();
//   const [majorCategories, setCategories] = useState<Category[]>([]);

//   // DB에 저장된 카테고리 불러오기
//   useEffect(() => {
//     axiosInstance.get("/categories")
//     .then(res => {
//       setCategories(res.data.data)
//       console.log(res.data.data)
//     })
//     .catch(err => {
//       console.log(err)
//       window.alert("카테고리 정보 조회 실패. 더미데이터가 출력됩니다.")
//       setCategories([
//         { id: 1, name: "채소류" },
//         { id: 12, name: "과일류" },
//         { id: 23, name: "곡물류" },
//         { id: 34, name: "견과류" },
//       ])
//     })
//   }, [])

const majorCategories = [
	{ id: 1, name: "채소류" },
	{ id: 2, name: "과일류" },
	{ id: 3, name: "곡물류" },
	{ id: 4, name: "견과류" },
	{ id: 5, name: "버섯류" },
	{ id: 6, name: "해조류" },
];

export default function CategoryLiveView() {
	const [lives, setLives] = useState<LiveListItem[]>([]);
	const [searchQuery, setSearchQuery] = useState("");
	const [searchType, setSearchType] = useState<"all" | "title" | "product" | "seller">("all");
	const [searchParams, setSearchParams] = useSearchParams();

	// 페이지네이션 관련 state
	const [totalElements, setTotalElements] = useState(0);
	const [loading, setLoading] = useState(false);

	// 종료된 경매 포함 여부
	const [includeEnded, setIncludeEnded] = useState(false);

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

	// URL에서 현재 선택된 카테고리 읽기
	const selectedCategory = useMemo(() => {
		const categoryParam = searchParams.get("category");
		if (categoryParam) {
			const categoryId = parseInt(categoryParam);
			if (majorCategories.some((cat) => cat.id === categoryId)) {
				return categoryId;
			}
		}
		return null;
	}, [searchParams]);

	// 카테고리, 페이지 변경 시 API 호출
	useEffect(() => {
		const fetchLives = async () => {
			setLoading(true);
			try {
				const params: Record<string, string | number | string[]> = {
					page: pagination.currentPage - 1, // Spring Boot는 0부터 시작
					size: pagination.pageSize,
					sortBy: "endDate",
					sortDirection: "ASC",
					endDateFrom: new Date().toISOString().replace("Z", "").split(".")[0], // 현재 시간 이후 종료되는 live만 조회
				};

				if (selectedCategory !== null) {
					params.categoryId = selectedCategory;
				}

				// 종료된 경매 포함 여부에 따른 상태 필터링
				if (!includeEnded) {
					params.statuses = ["SCHEDULED", "IN_PROGRESS"];
				}

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

				const normalized = response.data.data.content.map((live) => ({
					...live,
					auctions: live.auctions.map((a) => {
						const s = a.status?.toLowerCase() || "";
						const status: AuctionStatus =
							s === "in_progress" ? "active" : s === "ended" ? "closed" : s === "failed" ? "failed" : "scheduled";
						return { ...a, status };
					}),
				}));

				// 서버 페이징 사용
				setLives(normalized);
				setTotalElements(response.data.data.totalElements);
			} catch (err) {
				console.error("❌ 라이브 불러오기 실패:", err);
				setLives([]);
				setTotalElements(0);
			} finally {
				setLoading(false);
			}
		};

		fetchLives();
	}, [selectedCategory, pagination.currentPage, pagination.pageSize, includeEnded, searchQuery, searchType]);

	// 카테고리 선택 핸들러 - URL과 state 동시 업데이트
	const handleCategorySelect = (categoryId: number | null) => {
		setSearchParams(
			(params) => {
				if (categoryId === null) {
					params.delete("category");
				} else {
					params.set("category", categoryId.toString());
				}
				return params;
			},
			{ replace: true } // 뒤로가기 히스토리 오염 방지
		);
	};

	// 서버에서 이미 필터링된 데이터를 그대로 사용
	const visibleLives = lives;

	return (
		<div className="p-6 md:p-8 max-w-6xl mx-auto">
			<h1 className="text-2xl font-bold text-gray-800 text-center mt-5">카테고리별 경매 목록</h1>
			<p className="text-gray-800 text-center my-2">종류별로 원하는 상품을 구매하세요!</p>

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
					</div>

					{/* 옵션 영역 */}
					<div className="flex items-center gap-2 flex-shrink-0 w-[120px]">
						<div className="flex items-center gap-2 px-3 py-2 bg-gray-50 rounded-lg w-full">
							<input
								type="checkbox"
								id="includeEnded"
								checked={includeEnded}
								onChange={(e) => setIncludeEnded(e.target.checked)}
								className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
							/>
							<label htmlFor="includeEnded" className="text-sm text-gray-700 whitespace-nowrap">
								종료된 경매
							</label>
						</div>
					</div>
				</div>
			</div>

			<div className="flex flex-wrap gap-4 mt-8 border-b justify-center">
				<button
					className={`pb-2 px-4 h-8 flex items-center justify-center border-b-2 text-sm font-semibold ${
						selectedCategory === null
							? "border-orange-500 text-orange-500"
							: "border-transparent text-gray-500 hover:text-orange-400"
					}`}
					onClick={() => handleCategorySelect(null)}
				>
					전체
				</button>

				{majorCategories.map((cat) => (
					<button
						key={cat.id}
						className={`pb-2 px-4 h-8 flex items-center justify-center border-b-2 text-sm font-semibold ${
							selectedCategory === cat.id
								? "border-orange-500 text-orange-500"
								: "border-transparent text-gray-500 hover:text-orange-400"
						}`}
						onClick={() => handleCategorySelect(cat.id)}
					>
						{cat.name}
					</button>
				))}
			</div>

			{/* 로딩 상태 */}
			{loading && (
				<div className="flex justify-center mt-6">
					<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
				</div>
			)}

			<div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mt-6">
				{visibleLives.length > 0 ? (
					visibleLives.map((live) => <LiveCard key={live.id} live={live} />)
				) : (
					<p className="col-span-full text-center text-gray-400 mt-4">
						{loading ? "로딩 중..." : "해당 카테고리의 검색 결과가 없습니다."}
					</p>
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
