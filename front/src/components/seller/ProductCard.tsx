import dayjs from "dayjs";
import { FaTrash } from "react-icons/fa";

type ProductInfo = {
	id: number;
	name: string;
	origin: string;
	weight: string;
	reprImgSrc: string;
	description: string;
	grade: string;
	createdAt: Date;
	categoryId: number;
};

type ProductCardProps = {
	product: ProductInfo;
	isOwner?: boolean;
	onEdit?: (id: number) => void;
	onDelete?: (id: number) => void;
};

export default function ProductCard({
	product,
	isOwner,
	// onEdit,
	onDelete,
}: ProductCardProps) {
	// 등급별 색상 스타일
	const gradeBadgeStyles: Record<string, string> = {
		특: "bg-yellow-500 text-white",
		상: "bg-blue-500 text-white",
		중: "bg-green-500 text-white",
		하: "bg-gray-400 text-white",
	};

	return (
		<div className="flex bg-white rounded-lg shadow hover:shadow-md transition border border-gray-300 overflow-hidden relative">
			{/* 이미지 */}
			<div className="flex-shrink-0 w-32 h-32 bg-gray-100 overflow-hidden">
				<img
					src={product.reprImgSrc ? `data:image/jpeg;base64,${product.reprImgSrc}` : "/default.jpg"}
					alt={product.name}
					className="w-full h-full object-cover"
				/>
			</div>

			{/* 오른쪽 내용 */}
			<div className="flex flex-col flex-grow p-4 space-y-1 my-auto">
				{/* 우측 상단 버튼 */}
				{isOwner && (
					<div className="absolute top-2 right-2 flex space-x-2">
						{/* <button
              onClick={() => onEdit?.(product.id)}
              className="p-1 text-blue-600 hover:bg-blue-100 rounded cursor-pointer"
              aria-label="수정"
              title="수정"
            >
              <FaEdit className="h-4 w-4" />
            </button> */}

						<button
							onClick={() => onDelete?.(product.id)}
							className="p-1 text-red-500 hover:bg-red-100 rounded cursor-pointer"
							aria-label="삭제"
							title="삭제"
						>
							<FaTrash className="h-4 w-4" />
						</button>
					</div>
				)}

				{/* 상품명 + 등급 뱃지 */}
				<h3 className="text-lg font-semibold text-gray-800 truncate flex items-center gap-2">
					{product.name} {`(${product.weight} kg)`}
					<span
						className={`text-xs font-semibold px-2 py-1 rounded-full ${
							gradeBadgeStyles[product.grade] || "bg-gray-300 text-gray-700"
						}`}
					>
						{product.grade}
					</span>
				</h3>

				<p className="text-sm text-gray-600">{product.origin}</p>

				<p className="text-xs text-gray-400">등록일: {dayjs(product.createdAt).format("YYYY.MM.DD")}</p>
			</div>
		</div>
	);
}
