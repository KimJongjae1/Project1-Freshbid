import { useParams } from "react-router-dom";
import InquiryCard from "./InquiryCard";
import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import Pagination from "../pagination";
import { usePagination } from "../../hooks/usePagination";

type InquiryData = {
	id: number;
	writer: WriterInfo;
	content?: string;
	parentId: number;
	createdAt: Date;
	replies: InquiryData[];
};

type WriterInfo = {
	id: number;
	username: string;
	nickname: string;
};

type InquiryRequest = {
	sellerId: number;
	superId: number | null;
	content: string;
};

type InquiryProps = {
	isOwner: boolean;
};

const Inquiry = ({ isOwner }: InquiryProps) => {
	const { id } = useParams();

	const [inquiries, setInquiries] = useState<InquiryData[]>([]);
	const [inquiryContent, setInquiryContent] = useState<string>("");
	const [totalCount, setTotalCount] = useState<number>(0);
	const pagination = usePagination(totalCount, {
		initialPage: 1,
		pageSize: 8,
		blockSize: 10,
		enableUrlSync: false,
	});
	const fetchInquiries = async () => {
		try {
			const response = await axiosInstance.get(`/seller-info/${id}/qnas?pageNo=${pagination.currentPage - 1}`);

			setInquiries([...response.data.data.content]);
			setTotalCount(response.data.data.totalElements);
		} catch (error) {
			console.error(error);
		}
	};
	const submitInquiry = async (parentId: number | null, content: string) => {
		if (!content.trim()) {
			alert("입력란에 문의글을 입력하세요.");
			return;
		}
		if (!confirm("문의 글을 등록하시겠습니까?")) return;
		const request: InquiryRequest = {
			sellerId: Number(id!),
			superId: parentId,
			content: content,
		};
		try {
			const response = await axiosInstance.post(`/auction/qna`, request);
			console.log(response.data);

			fetchInquiries();
			alert("문의 글이 등록되었습니다.");
			setInquiryContent("");
		} catch (error) {
			console.error(error);
		}
	};
	useEffect(() => {
		fetchInquiries();
	}, [id, pagination.currentPage]);

	return (
		<div className="mt-10 space-y-2">
			{/* 새 문의 글 작성 폼 */}
			<div className="border border-gray-200 rounded p-3 bg-white">
				<textarea
					value={inquiryContent}
					onChange={(e) => setInquiryContent(e.target.value)}
					rows={3}
					className="w-full border border-gray-300 rounded p-2 text-sm focus:outline-none focus:border-green-400"
					placeholder="판매자에게 문의해보세요"
				/>
				<div className="mt-2 flex justify-end gap-2">
					<button
						onClick={() => submitInquiry(null, inquiryContent)}
						className="px-3 py-1 text-xs rounded bg-green-500 text-white hover:bg-breen-600"
					>
						등록
					</button>
				</div>
			</div>
			{/* 상단: 문의 작성 버튼 + 총 개수 */}
			<div className="flex items-center justify-between">
				<p className="text-sm text-gray-500 mr-1">
					총 <span className="font-semibold text-gray-700">{totalCount}</span>
					개의 문의 글이 등록되어 있습니다.
				</p>
			</div>

			{/* 문의 카드 목록 또는 비어 있을 경우 문구 */}
			{inquiries.length === 0 ? (
				<p className="text-center text-sm text-gray-400 py-4">작성된 문의가 없습니다.</p>
			) : (
				inquiries.map((item) => (
					<InquiryCard
						isOwner={isOwner}
						inquiry={item}
						onReplySubmit={(parentId, content) => submitInquiry(parentId, content)}
					/>
				))
			)}

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
		</div>
	);
};

export default Inquiry;
