import { useState } from "react";
import axiosInstance from "../api/axiosInstance";
import { useNavigate } from "react-router";

interface UserSearchResponse {
	id: number;
	profileImage: string | null;
	username: string;
	nickname: string;
}

// 새로운 SearchBar 컴포넌트
type SearchBarProps = {
	onSearch: (query: string) => void;
};

function SearchBar({ onSearch }: SearchBarProps) {
	const [input, setInput] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (input.trim().length < 2) {
			alert("검색어는 2글자 이상 입력해주세요.");
			return;
		}
		onSearch(input.trim());
	};

	return (
		<form onSubmit={handleSubmit} className="w-[45vw] min-w-[250px] max-w-md mx-auto">
			<div className="flex items-center border border-gray-300 rounded-full px-4 py-2 shadow-sm">
				<input
					type="text"
					value={input}
					onChange={(e) => setInput(e.target.value)}
					placeholder="닉네임 또는 아이디 검색 (2자 이상 입력)"
					className="flex-grow text-sm outline-none placeholder-gray-400"
				/>
				<button type="submit" className="text-gray-500 hover:text-gray-700">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						strokeWidth="1.5"
						stroke="currentColor"
						className="w-5 h-5"
					>
						<path
							strokeLinecap="round"
							strokeLinejoin="round"
							d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"
						/>
					</svg>
				</button>
			</div>
		</form>
	);
}

export default function SellerSearch() {
	const [sellerList, setSellerList] = useState<UserSearchResponse[]>([]);
	const navigate = useNavigate();
	const [notFound, setNotFound] = useState<boolean>(false);
	const [hasSearched, setHasSearched] = useState<boolean>(false);

	const fetchSellers = async (query: string) => {
		setHasSearched(true);
		try {
			const response = await axiosInstance(`/seller-info/search?query=${encodeURIComponent(query)}`);
			setSellerList(response.data.data);
			setNotFound(response.data.data.length === 0);
		} catch (error) {
			console.error(error);
			setNotFound(true);
			setSellerList([]);
		}
	};

	return (
		<div className="space-y-6 p-4">
			{/* 검색 영역 - 새로운 디자인 적용 */}
			<div className="w-full">
				<SearchBar onSearch={fetchSellers} />
			</div>

			{/* 검색 결과 */}
			{hasSearched && (
				<>
					{sellerList.length > 0 ? (
						<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
							{sellerList.map((seller: UserSearchResponse) => (
								<div
									key={seller.id}
									className="bg-white p-4 rounded-lg shadow border border-gray-200 hover:border-green-500 hover:shadow-md transition-all cursor-pointer"
									onClick={() => {
										navigate(`/seller/detail/${seller.id}`);
									}}
								>
									<div className="flex items-center gap-3">
										<img
											src={
												seller.profileImage ? `data:image/jpeg;base64,${seller.profileImage}` : "/farmer-profile.png"
											}
											alt="프로필"
											className="w-12 h-12 rounded-full object-cover"
										/>
										<div>
											<p className="font-bold">{seller.nickname}</p>
											<p className="text-sm text-gray-500">{seller.username}</p>
										</div>
									</div>
								</div>
							))}
						</div>
					) : (
						notFound && (
							<div className="text-center py-12">
								<div className="text-gray-500 text-lg">검색 결과가 없습니다</div>
								<p className="text-gray-400 text-sm mt-2">다른 검색어로 시도해보세요</p>
							</div>
						)
					)}
				</>
			)}
		</div>
	);
}
