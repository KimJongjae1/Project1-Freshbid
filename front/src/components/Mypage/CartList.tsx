/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState, useEffect } from "react";
import axiosInstance from "../../api/axiosInstance";

// 장바구니 아이템 타입 정의
interface CartItem {
	id: number;
	product: string;
	quantity: number;
	price: number;
	seller: string;
	img?: string;
	origin?: string;
	status: string;
	regDate: string;
	reprImgSrc: string | null;
	auctionTitle?: string;
}

const CartList = () => {
	const [cartItems, setCartItems] = useState<CartItem[]>([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState<string | null>(null);

	// API에서 장바구니 목록 가져오기 (WAITING 상태의 주문들)
	const fetchCartlist = async () => {
		try {
			setLoading(true);
			setError(null);

			// 주문 목록 조회
			const response = await axiosInstance.get("/orders");
			const orderData = response.data.data || [];

			console.log("주문 데이터:", orderData);

			// WAITING 상태의 주문들만 필터링하여 장바구니로 표시
			const waitingOrders = orderData.filter((order: any) => order.status === "WAITING");

			// OrderResponseDto는 단순한 구조이므로, 상세 정보를 위해 개별 주문 조회
			const detailedOrders = await Promise.all(
				waitingOrders.map(async (order: any) => {
					try {
						const detailResponse = await axiosInstance.get(`/orders/${order.id}`);
						const detailData = detailResponse.data.data;

						// OrderDetailResponseDto에는 sellerId가 없으므로 원본 데이터의 sellerId를 추가
						return {
							...detailData,
							sellerId: order.sellerId, // 원본 OrderResponseDto의 sellerId 보존
						};
					} catch (err) {
						console.error(`주문 ${order.id} 상세 조회 실패:`, err);
						return order; // 상세 조회 실패 시 기본 데이터 사용
					}
				})
			);

			// API 응답을 CartItem 형태로 변환
			const mappedData = detailedOrders.map((order: any) => ({
				id: order.id,
				product: order.product?.name || "상품명 없음",
				quantity: 1, // 주문은 1개씩
				price: order.price || 0,
				seller: order.product?.user?.nickname || order.product?.user?.username || "판매자",
				img: order.product?.reprImgSrc,
				origin: order.product?.origin,
				status: order.status,
				regDate: order.createdAt,
				reprImgSrc: order.product?.reprImgSrc,
				auctionTitle: order.product?.name
					? `${order.product.name} (경매 #${order.auctionId})`
					: `경매 #${order.auctionId}`,
			}));

			setCartItems(mappedData);
		} catch (err: any) {
			console.error("장바구니 조회 실패:", err);
			setError(err.response?.data?.message || "장바구니를 불러오는데 실패했습니다.");
		} finally {
			setLoading(false);
		}
	};

	// 장바구니에서 삭제 (주문 취소)
	const handleRemoveFromCart = async () => {
		alert("취소는 판매자에게 문의해주세요.");
	};

	// 결제하기
	const handlePayment = async () => {
		alert("결제 기능은 개발 중입니다.");
	};

	useEffect(() => {
		fetchCartlist();
	}, []);

	// 총 금액 계산
	const totalAmount = cartItems.reduce((total, item) => total + item.price * item.quantity, 0);
	const totalItems = cartItems.length;

	if (loading) {
		return (
			<div className="flex justify-center items-center p-8 min-h-screen min-w-[500px]">
				<div className="flex flex-col items-center gap-4">
					<div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"></div>
					<div className="text-gray-600 text-lg">장바구니를 불러오는 중...</div>
				</div>
			</div>
		);
	}

	if (error) {
		return (
			<div className="min-h-screen flex items-center justify-center p-4 min-w-[500px]">
				<div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-lg max-w-md text-center">
					<div className="text-lg font-semibold mb-2">오류가 발생했습니다</div>
					<div>{error}</div>
				</div>
			</div>
		);
	}

	return (
		<div className="min-h-screen min-w-[500px]">
			{/* 헤더 */}
			<div className="bg-white">
				<div className="max-w-7xl mx-auto px-4 min-w-[500px]">
					<p className="text-gray-600 mt-1">총 {totalItems}개의 상품</p>
				</div>
			</div>

			<div className="max-w-7xl mx-auto px-4 py-6 min-w-[500px]">
				{cartItems.length === 0 ? (
					<div className="text-center py-16">
						<div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
							<svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									strokeLinecap="round"
									strokeLinejoin="round"
									strokeWidth="2"
									d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.8 1.8M7 13l1.8 1.8m0 0L12 18m0 0l3.2-3.2M12 18v-6"
								/>
							</svg>
						</div>
						<h3 className="text-lg font-semibold text-gray-900 mb-2">장바구니가 비어있습니다</h3>
						<p className="text-gray-500">경매에서 낙찰받은 상품들이 여기에 표시됩니다.</p>
					</div>
				) : (
					<div className="flex flex-col md:flex-row gap-8 relative">
						{/* 왼쪽: 상품 목록 - 모바일에서는 위쪽 */}
						<div className="flex-1 md:pr-4">
							<div className="space-y-4">
								{cartItems.map((item) => (
									<div
										key={item.id}
										className="bg-white min-w-[400px] rounded-xl shadow-sm border border-gray-200 p-4 md:p-6 hover:shadow-md transition-all duration-200"
									>
										<div className="flex flex-col sm:flex-row gap-4 md:gap-6">
											{/* 상품 이미지 */}
											<div className="w-full sm:w-28 h-28 bg-gray-100 rounded-lg flex items-center justify-center border border-gray-200 overflow-hidden">
												{item.reprImgSrc ? (
													<img src={`data:image/jpeg;base64,${item.reprImgSrc}`} className="w-full h-full object-cover" />
												) : (
													<div className="flex flex-col items-center text-gray-400">
														<svg className="w-8 h-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
															<path
																strokeLinecap="round"
																strokeLinejoin="round"
																strokeWidth="2"
																d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
															/>
														</svg>
														<span className="text-xs">이미지</span>
													</div>
												)}
											</div>

											{/* 상품 정보 */}
											<div className="flex-1">
												<div className="flex flex-col sm:flex-row sm:justify-between sm:items-start mb-3">
													<div className="mb-2 sm:mb-0">
														<h3 className="text-lg font-semibold text-gray-900 mb-1" title={item.product}>
															{item.product}
														</h3>
														{item.auctionTitle && <p className="text-sm text-blue-600 mb-2">🎯 {item.auctionTitle}</p>}
													</div>
													<div className="text-left sm:text-right">
														<p className="text-xl font-bold text-green-600">
															{(item.price * item.quantity).toLocaleString()}원
														</p>
														<p className="text-sm text-gray-500">수량: {item.quantity}</p>
													</div>
												</div>

												{/* 판매자 정보 */}
												<div className="flex flex-wrap items-center gap-2 mb-4">
													<span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
														판매자: {item.seller}
													</span>
													{item.origin && (
														<span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
															원산지: {item.origin}
														</span>
													)}
												</div>

												{/* 액션 버튼 */}
												<div className="flex gap-3">
													<button
														onClick={handlePayment}
														className="flex-1 bg-green-600 text-white py-2.5 px-4 rounded-lg font-medium hover:bg-green-700 transition-colors duration-200 flex items-center justify-center gap-2"
													>
														<svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
															<path
																strokeLinecap="round"
																strokeLinejoin="round"
																strokeWidth="2"
																d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
															/>
														</svg>
														결제하기
													</button>
													<button
														onClick={handleRemoveFromCart}
														className="px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors duration-200 flex items-center justify-center gap-2"
													>
														<svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
															<path
																strokeLinecap="round"
																strokeLinejoin="round"
																strokeWidth="2"
																d="M6 18L18 6M6 6l12 12"
															/>
														</svg>
														취소
													</button>
												</div>
											</div>
										</div>
									</div>
								))}
							</div>
						</div>

						{/* 오른쪽: 주문 요약 (고정) - 모바일에서는 아래쪽 */}
						<div className="w-full md:w-70">
							<div className="md:sticky md:top-6">
								<div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
									<h2 className="text-lg font-bold text-gray-900 mb-6">주문 요약</h2>

									{/* 상품 개수 */}
									<div className="flex justify-between items-center mb-4 pb-4 border-b border-gray-100">
										<span className="text-gray-600">상품 개수</span>
										<span className="font-semibold text-gray-900">{totalItems}개</span>
									</div>

									{/* 총 상품 금액 */}
									<div className="flex justify-between items-center mb-4 pb-4 border-b border-gray-100">
										<span className="text-gray-600">총 상품 금액</span>
										<span className="font-semibold text-gray-900">{totalAmount.toLocaleString()}원</span>
									</div>

									{/* 배송비 */}
									<div className="flex justify-between items-center mb-6 pb-6 border-b border-gray-200">
										<span className="text-gray-600">배송비</span>
										<span className="font-semibold text-green-600">무료</span>
									</div>

									{/* 최종 결제 금액 */}
									<div className="flex justify-between items-center mb-6">
										<span className="text-lg font-bold text-gray-900">총 결제 금액</span>
										<span className="text-2xl font-bold text-green-600">{totalAmount.toLocaleString()}원</span>
									</div>

									{/* 전체 결제 버튼 */}
									<button
										onClick={handlePayment}
										className="w-full bg-green-600 text-white py-4 px-6 rounded-lg text-lg font-bold hover:bg-green-700 transition-colors duration-200 flex items-center justify-center gap-2 shadow-lg"
									>
										<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path
												strokeLinecap="round"
												strokeLinejoin="round"
												strokeWidth="2"
												d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
											/>
										</svg>
										전체 상품 결제하기
									</button>

									{/* 추가 정보 */}
									<div className="mt-6 p-4 bg-blue-50 rounded-lg">
										<div className="flex items-start gap-3">
											<svg
												className="w-5 h-5 text-blue-500 mt-0.5"
												fill="none"
												stroke="currentColor"
												viewBox="0 0 24 24"
											>
												<path
													strokeLinecap="round"
													strokeLinejoin="round"
													strokeWidth="2"
													d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
												/>
											</svg>
											<div>
												<p className="text-sm font-medium text-blue-900 mb-1">안전한 거래</p>
												<p className="text-xs text-blue-700">모든 거래는 안전하게 보호됩니다</p>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				)}
			</div>
		</div>
	);
};

export default CartList;
