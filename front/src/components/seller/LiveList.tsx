import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import LiveCard from "../Auction/LiveCard";
import { usePagination } from "../../hooks/usePagination";
import Pagination from "../pagination";
import { useNavigate } from "react-router";

type LiveListItem = {
	id: number;
	title: string;
	reprImgSrc: string | null;
	startDate: string;
	endDate: string;
	liveStatus: LiveStatus;
	seller: {
		sellerId: number;
		nickname: string;
	};
	auctions: {
		product: {
			imageUrl: string;
		};
	}[];
};

type LiveStatus = "SCHEDULED" | "IN_PROGRESS" | "ENDED";

// SearchBar 컴포넌트
function SearchBar({ onSearch }: { onSearch: (query: string) => void }) {
	const [input, setInput] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		onSearch(input);
	};

	return (
		<form onSubmit={handleSubmit} className="w-full">
			<div className="flex items-center border border-gray-300 rounded-lg px-4 py-2 shadow-sm">
				<input
					type="text"
					value={input}
					onChange={(e) => setInput(e.target.value)}
					placeholder="검색할 내용을 입력하세요"
					className="flex-grow text-sm outline-none placeholder-gray-400"
				/>
				<button type="submit" className="ml-2 text-gray-500 hover:text-gray-700">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						className="h-5 w-5"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							strokeLinecap="round"
							strokeLinejoin="round"
							strokeWidth={2}
							d="M21 21l-4.35-4.35M10 18a8 8 0 100-16 8 8 0 000 16z"
						/>
					</svg>
				</button>
			</div>
		</form>
	);
}

type LiveListProps = {
	id: string;
	isOwner: boolean;
};

export default function LiveList({ id, isOwner }: LiveListProps) {
	const [liveList, setLiveList] = useState<LiveListItem[]>([]);
	const [totalCount, setTotalCount] = useState<number>(0);
	const [searchQuery, setSearchQuery] = useState<string>("");
	const [includeEnded, setIncludeEnded] = useState<boolean>(false);
	const navigate = useNavigate();
	const pagination = usePagination(totalCount, {
		initialPage: 1,
		pageSize: 9,
		blockSize: 10,
		enableUrlSync: false,
	});

	useEffect(() => {
		const fetchSellerLives = async () => {
			try {
				let url = `/seller-info/${id}/lives?pageNo=${pagination.currentPage - 1}&isEnded=${includeEnded}`;

				// 검색 쿼리 추가
				if (searchQuery.trim()) {
					const encodedQuery = encodeURIComponent(searchQuery.trim());
					url += `&title=${encodedQuery}`;
				}

				const response = await axiosInstance(url);
				// console.log(response.data.data);

				// 백엔드에서 받은 status 필드를 liveStatus로 매핑
				const mappedLives = response.data.data.content.map((live: Record<string, unknown>) => ({
					...live,
					liveStatus: live.status as string, // status를 liveStatus로 매핑
				}));

				setLiveList(mappedLives);
				setTotalCount(response.data.data.totalElements);
			} catch (error) {
				console.error(error);
			}
		};
		fetchSellerLives();
	}, [id, pagination.currentPage, searchQuery, includeEnded]);

	// CategoryAuctionView와 동일한 정렬 및 필터링 로직
	// const sortedAndFilteredLives = useMemo(() => {
	// 	const now = new Date();

	// 	// 1. 현재 시간 이후 종료되는 라이브만 필터링 (CategoryAuctionView와 동일)
	// 	const filtered = liveList.filter(live => {
	// 		const endDate = new Date(live.endDate);
	// 		return endDate > now;
	// 	});

	// 	// 2. endDate 기준 오름차순 정렬 (CategoryAuctionView와 동일)
	// 	return filtered.sort((a, b) => {
	// 		const endDateA = new Date(a.endDate);
	// 		const endDateB = new Date(b.endDate);
	// 		return endDateA.getTime() - endDateB.getTime(); // ASC (빨리 끝나는 순)
	// 	});
	// }, [liveList]);

	return (
		<>
			{/* 상단 검색 영역 */}
			<div className="w-full max-w-4xl mx-auto mb-6">
				<div className="flex gap-3 items-center bg-white p-4 rounded-lg">
					{/* 검색 영역 */}
					<div className="flex flex-1 gap-2 items-center min-w-0">
						{/* 검색바 */}
						<div className="flex-1 min-w-0">
							<SearchBar onSearch={setSearchQuery} />
						</div>
					</div>

					{/* 옵션 영역 */}
					<div className="flex items-center gap-2 flex-shrink-0 w-[140px]">
						<div className="flex items-center gap-2 px-3 py-2 bg-gray-50 rounded-lg w-full">
							<input
								type="checkbox"
								id="includeEnded"
								checked={includeEnded}
								onChange={(e) => setIncludeEnded(e.target.checked)}
								className="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300 rounded"
							/>
							<label htmlFor="includeEnded" className="text-sm text-gray-700 whitespace-nowrap">
								종료된 라이브
							</label>
						</div>
					</div>
				</div>
			</div>

			{/* 라이브 추가 버튼 */}
			{isOwner && (
				<div className="flex justify-end mb-4">
					<button
						onClick={() => navigate("/auction/create")}
						className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition cursor-pointer"
					>
						라이브 추가
					</button>
				</div>
			)}

			{/* 라이브 목록 */}
			{liveList.length > 0 ? (
				<div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 justify-center px-2 mt-5">
					{liveList?.map((live) => (
						<LiveCard key={live.id} live={live} />
					))}
				</div>
			) : (
				<div className="text-2xl flex items-center justify-center h-72">라이브가 존재하지 않습니다</div>
			)}

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
						disabled={false}
						className="mt-4"
					/>
				</div>
			)}
		</>
	);
}
