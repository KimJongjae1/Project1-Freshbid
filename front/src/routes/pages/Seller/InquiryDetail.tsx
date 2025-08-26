import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useEffect } from "react";

export default function InquiryDetail() {
	const { id } = useParams(); // seller ID
	const { state } = useLocation();
	const navigate = useNavigate();

	useEffect(() => {
		window.scrollTo(0, 0);
	}, []);

	if (!state) {
		return <p className="p-8">잘못된 접근입니다.</p>;
	}

	const {
		title,
		// author,
		date,
		// isPrivate,
		hasReply,
		replyContent,
		replyAuthor,
		replyDate,
		farmName, // 넘어온 경우
		content,
	} = state;

	return (
		<div className="p-8 space-y-6 max-w-3xl mx-auto">
			<h2 className="text-2xl font-semibold border-b pb-2">{title}</h2>
			<p className="text-sm text-gray-500">{date}</p>

			<div className="border p-4 rounded text-gray-800 whitespace-pre-line">{content}</div>

			{hasReply && (
				<div className="bg-green-50 border-l-4 border-green-300 p-4 rounded text-sm text-gray-700">
					<p className="font-semibold text-gray-800 mb-1">
						{replyAuthor} <span className="text-xs text-gray-500">({replyDate})</span>
					</p>
					<p>{replyContent}</p>
				</div>
			)}

			<div className="pt-6">
				<button
					onClick={() =>
						navigate(`/seller/detail/${id}`, {
							state: {
								tab: "문의",
								farmName: farmName || "이름 없는 농장",
							},
						})
					}
					className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
				>
					목록
				</button>
			</div>
		</div>
	);
}
