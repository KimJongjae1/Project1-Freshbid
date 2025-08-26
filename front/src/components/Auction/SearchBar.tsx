import { useState } from "react";

type SearchBarProps = {
	onSearch: (query: string) => void;
};

export default function SearchBar({ onSearch }: SearchBarProps) {
	const [input, setInput] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		onSearch(input.trim());
	};

	return (
		<form onSubmit={handleSubmit} className="w-[45vw] min-w-[250px] max-w-md mx-auto">
			<div className="flex items-center border border-gray-300 rounded-full px-4 py-2 shadow-sm">
				<input
					type="text"
					value={input}
					onChange={(e) => setInput(e.target.value)}
					placeholder="검색할 품목을 입력하세요"
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
