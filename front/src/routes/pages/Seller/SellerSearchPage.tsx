import SellerSearch from "../../../components/SellerSearch";
export default function SellerSearchPage() {
	return (
		<div className="p-6 md:p-8 max-w-6xl mx-auto">
			<h1 className="text-2xl font-bold text-gray-800 text-center mt-5">판매자 검색</h1>
			<p className="text-gray-800 text-center my-2">다양한 상품을 파는 상품 판매자 정보를 조회해보세요!</p>
			<SellerSearch />
		</div>
	);
}
