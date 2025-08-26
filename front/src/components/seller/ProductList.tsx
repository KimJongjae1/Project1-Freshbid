import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import ProductCard from "./ProductCard";
import Pagination from "../pagination";
import { usePagination } from "../../hooks/usePagination";
import { useNavigate } from "react-router";

const majorCategories = [
	{ id: 0, name: "전체", img: "" },
	{ id: 1, name: "채소류", img: "/category-vegetable.png" },
	{ id: 2, name: "과일류", img: "/category-fruit.png" },
	{ id: 3, name: "곡물류", img: "/category-wheat.png" },
	{ id: 4, name: "견과류", img: "/category-nuts.png" },
	{ id: 5, name: "버섯류", img: "/category-mushroom.png" },
	{ id: 6, name: "해조류", img: "/category-seaweed.png" },
];

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

type ProductListProps = {
	id: number;
	isOwner: boolean;
};
export default function ProductList({ id, isOwner }: ProductListProps) {
	const [selectedCategory, setSelectedCategory] = useState<number>(0);
	const [totalCount, setTotalCount] = useState<number>(0);
	const [productList, setProductList] = useState<ProductInfo[]>([]);
	const pagination = usePagination(totalCount, {
		initialPage: 1,
		pageSize: 8,
		blockSize: 10,
		enableUrlSync: false,
	});
	const navigate = useNavigate();
	const fetchProducts = async () => {
		try {
			let url = `/seller-info/${id}/products?pageNo=${pagination.currentPage - 1}`;
			if (selectedCategory > 0) {
				url += `&category=${selectedCategory}`;
			}
			const response = await axiosInstance.get(url);
			setProductList([...response.data.data.content]);
			setTotalCount(response.data.data.totalElements);
		} catch (error) {
			console.error(error);
		}
	};
	const deleteProduct = async (productId: number) => {
		if (!confirm("상품을 삭제하시겠습니까?")) return;
		try {
			await axiosInstance.delete(`/auction/product/${productId}`);

			alert("삭제되었습니다.");
			fetchProducts();
		} catch (error) {
			console.error(error);
		}
	};
	useEffect(() => {
		fetchProducts();
	}, [selectedCategory, pagination.currentPage]);

	return (
		<>
			{isOwner && (
				<button
					onClick={() => navigate("/product/create")}
					className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition block ml-auto cursor-pointer"
				>
					판매 상품 추가
				</button>
			)}
			{/*카테고리 목록 */}
			<div className="mt-2 p-2 flex justify-between">
				{majorCategories.map((category) => (
					<button
						key={category.id}
						onClick={() => setSelectedCategory(category.id)}
						className={`flex justify-center gap-2 w-1/7 border-b-2 border-gray-300 p-2 cursor-pointer hover:bg-green-500 hover:text-white
                        ${selectedCategory === category.id ? "bg-green-500 text-white border-green-500" : ""}`}
					>
						{category.id > 0 && <img src={category.img} className="w-8 h-8" />}
						<span className="my-auto text-base">{category.name}</span>
					</button>
				))}
			</div>
			{/*상품들 */}
			<div className="mt-5 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 gap-4">
				{productList.map((product) => (
					<ProductCard
						product={product}
						isOwner={isOwner}
						onEdit={() => console.log("edit")}
						onDelete={() => deleteProduct(product.id)}
					/>
				))}
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
						disabled={false}
						className="mt-4"
					/>
				</div>
			)}
		</>
	);
}
