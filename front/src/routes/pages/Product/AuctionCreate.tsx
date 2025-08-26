import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import AuctionCard from "../../../components/Auction/AuctionCard";
import axiosInstance from "../../../api/axiosInstance";
import { useUserStore } from "../../../stores/useUserStore";

interface Product {
	id: number;
	name: string;
	origin: string;
	weight: string;
	username: string | null;
	description: string;
	reprImgSrc: string;
	categoryId: number;
}

interface AuctionProduct {
	productId: number;
	amount: number;
	startPrice: number;
	selected: boolean;
}

const AuctionCreate = () => {
	const navigate = useNavigate();
	const [title, setTitle] = useState("");
	const [products, setProducts] = useState<Product[]>([]);
	const [auctionProducts, setAuctionProducts] = useState<AuctionProduct[]>([]);

	const now = new Date();
	const offset = now.getTimezoneOffset() * 60000;
	const today = new Date(now.getTime() - offset);

	const [startDate, setStartDate] = useState(() => {
		return today.toISOString().slice(0, 16);
	});
	const [endDate, setEndDate] = useState(() => {
		today.setHours(today.getHours() + 1);
		return today.toISOString().slice(0, 16);
	});
	const [isLoading, setIsLoading] = useState(true);

	// 업로드할 이미지 파일, 미리보기
	const [imgFile, setImgFile] = useState<File | null>(null);
	const [previewImage, setPreviewImage] = useState<string | null>(null);
	const MAX_FILE_SIZE = 10 * 1024 * 1024;
	// 판매자의 상품 목록 가져오기 (실제로는 API 호출)
	useEffect(() => {
		const fetchProducts = async () => {
			try {
				// 전체 상품 중 username이 일치하는 상품만 라이브 등록 가능
				await axiosInstance("/auction/product").then((res) => {
					const allProducts: Product[] = res.data.data.content;
					const myProducts: Product[] = allProducts.filter((product) => {
						return product.username === useUserStore.getState().username;
					});
					const initialAuctionProducts = myProducts.map((product) => ({
						productId: product.id,
						startPrice: 10000,
						amount: 1,
						selected: false,
					}));
					setAuctionProducts(initialAuctionProducts);
					setProducts(myProducts);
					setIsLoading(false);
				});
			} catch (error) {
				console.error("상품 목록을 불러오는 중 오류가 발생했습니다:", error);
				// 임시 데이터
				const tempUser = useUserStore.getState().username;
				const mockProducts: Product[] = [
					{
						id: 1,
						name: "신선한 사과",
						origin: "경북 경산",
						weight: "10kg 박스",
						username: tempUser,
						description: "경북 경산에서 재배한 신선한 사과입니다. 달콤한 맛이 일품입니다.",
						reprImgSrc: "/sample-apple.jpg",
						categoryId: 1,
					},
					{
						id: 2,
						name: "맛있는 배",
						origin: "전남 나주",
						weight: "5kg 박스",
						username: tempUser,
						description: "전남 나주 특산품 배입니다. 시원하고 달콤한 맛이 특징입니다.",
						reprImgSrc: "/sample-pear.jpg",
						categoryId: 1,
					},
					{
						id: 3,
						name: "제철 딸기",
						origin: "경남 진주",
						weight: "2kg 상자",
						username: tempUser,
						description: "경남 진주에서 수확한 제철 딸기입니다. 상큼한 맛이 특징입니다.",
						reprImgSrc: "/sample-strawberry.jpg",
						categoryId: 1,
					},
				];

				const initialAuctionProducts = mockProducts.map((product) => ({
					productId: product.id,
					startPrice: 10000,
					amount: 1,
					selected: false,
				}));
				setAuctionProducts(initialAuctionProducts);
				setProducts(mockProducts);
				setIsLoading(false);
			}
		};

		fetchProducts();
	}, []);

	// 상품 선택 상태 변경
	const handleProductSelection = (productId: number) => {
		setAuctionProducts((prevProducts) =>
			prevProducts.map((product) =>
				product.productId === productId ? { ...product, selected: !product.selected } : product
			)
		);
	};

	// 상품 수량 변경
	const handleQuantityChange = (productId: number, amount: number) => {
		if (amount < 1) return;

		setAuctionProducts((prevProducts) =>
			prevProducts.map((product) => (product.productId === productId ? { ...product, amount } : product))
		);
	};


	// 상품 시작가 변경
	const handleStartPriceChange = (productId: number, price: number) => {
		setAuctionProducts((prevProducts) =>
			prevProducts.map((product) => (product.productId === productId ? { ...product, startPrice: price } : product))
		);
	};

	const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (file) {
			if (file.size > MAX_FILE_SIZE) {
				alert("파일 크기는 10MB를 초과할 수 없습니다.");
				e.target.value = "";
				return;
			}
			setImgFile(file);
			setPreviewImage(URL.createObjectURL(file)); // 미리보기 URL
		}
	}
	// 폼 제출
	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();

		// 선택된 상품만 필터링
		const selectedProducts = auctionProducts.filter((product) => product.selected);


		if (!startDate || !endDate) {
			alert("경매 시작 시간과 종료 시간을 모두 입력해주세요.");
			return;
		}

		// 시작 시간이 현재 시간보다 이후인지 확인
		const now = new Date();
		const startDateTime = new Date(startDate);
		const endDateTime = new Date(endDate);

		if (startDateTime < now) {
			console.log(now);
			console.log(startDateTime);
			alert("경매 시작 시간은 현재 시간 이후로 설정해야 합니다.");
			return;
		}

		if (endDateTime <= startDateTime) {
			alert("경매 종료 시간은 시작 시간 이후로 설정해야 합니다.");
			return;
		}

		if (selectedProducts.length === 0) {
			alert("최소 1개 이상의 상품을 등록해 주세요.")
			return;
		}

		// 경매 생성 데이터
		const auctionData = {
			title,
			startDate,
			endDate,
			auctionsJson: JSON.stringify(
				selectedProducts.map(product => ({
				productId: product.productId,
				amount: product.amount,
				startPrice: product.startPrice
				}))
			),
			imgFile
		};

		console.log("경매 생성 데이터:", auctionData);

		// TODO: 경매 생성 API 호출
		axiosInstance
			.post("/auction/live", auctionData, {
				headers: { "Content-Type": "multipart/form-data" },
			})
			.then((res) => {
				if (res.status === 201) {
					window.alert("라이브 등록 성공!");
					// 등록 성공 후 마이페이지로 리다이렉트
					navigate(-1);
				} else {
					window.alert("에러 발생");
					console.log(res.headers);
				}
			})
			.catch((error) => {
				window.alert("에러 발생");
				console.log(error);
			});
	};

	return (
		<div className="max-w-4xl mx-auto px-4 py-8">
			<h1 className="text-2xl font-bold text-center mb-8">실시간 라이브 생성하기</h1>

			{isLoading ? (
				<div className="text-center py-10">
					<p>상품 목록을 불러오는 중...</p>
				</div>
			) : (
				<form onSubmit={handleSubmit} className="space-y-8">
					{/* 제목 설정 */}
					<div className="bg-gray-50 p-6 rounded-lg">
						<h2 className="text-xl font-medium mb-4">라이브 제목</h2>
						<div>
							<input
								type="text"
								id="title"
								value={title}
								onChange={(e) => setTitle(e.target.value)}
								required
								className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>
					</div>
					{/*경매 이미지 업로드 (선택 사항) */}
					<div className="bg-gray-50 p-6 rounded-lg">
						<h2 className="text-xl font-medium mb-4">라이브 이미지 등록</h2>
						<div>
							<input
								type="file"
								accept="image/*"
								id="img"
								onChange={handleImageChange}
								className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>
						<div className="mt-4">
							{previewImage && (
							<img
								src={previewImage}
								alt="미리보기"
								className="w-full max-h-64 object-contain rounded-md"
							/>
							)}
						</div>
					</div>
					{/* 경매 시간 설정 */}
					<div className="bg-gray-50 p-6 rounded-lg">
						<h2 className="text-xl font-medium mb-4">라이브 시간 설정</h2>
						<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
							<div>
								<label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
									라이브 시작 시간 <span className="text-red-500">*</span>
								</label>
								<input
									type="datetime-local"
									id="startDate"
									value={startDate}
									onChange={(e) => setStartDate(e.target.value)}
									required
									className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
								/>
							</div>
							<div>
								<label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-1">
									라이브 종료 시간 <span className="text-red-500">*</span>
								</label>
								<input
									type="datetime-local"
									id="endDate"
									value={endDate}
									onChange={(e) => setEndDate(e.target.value)}
									required
									className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
								/>
							</div>
						</div>
					</div>

					{/* 상품 선택 */}
					<div className="bg-gray-50 p-6 rounded-lg">
						<h2 className="text-xl font-medium mb-4">판매할 상품 선택</h2>

						{products.length === 0 ? (
							<div className="text-center py-8 bg-white rounded-lg border border-gray-200">
								<p className="text-gray-500">등록된 상품이 없습니다.</p>
								<button
									type="button"
									onClick={() => navigate("/product/create")}
									className="mt-3 px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition"
								>
									상품 등록하기
								</button>
							</div>
						) : (
							<div className="space-y-4">
								{products.map((product) => {
									const auctionProduct = auctionProducts.find((ap) => ap.productId === product.id);

									if (!auctionProduct) return null;

									return (
										<div key={product.id} className={`mb-4 ${auctionProduct.selected ? "ring-2 ring-blue-500" : ""}`}>
											<div className="relative">
												<div className="absolute top-4 right-4 z-10">
													<input
														type="checkbox"
														id={`product-${product.id}`}
														checked={auctionProduct.selected}
														// onClick={() => handleProductSelection(product.id)}
														onChange={(e) => {
															e.stopPropagation();
															handleProductSelection(product.id);
														}}
														className="h-5 w-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
													/>
												</div>

												<AuctionCard
													imageUrl={product.reprImgSrc || "/default.jpg"}
													name={product.name}
													description={`규격: ${product.weight} | ${product.description || ""}`}
													auctionDate="경매 예정"
													category={`원산지: ${product.origin}`}
													badges={[]}
												/>
											</div>

											{auctionProduct.selected && (
												<div className="mt-4 p-4 bg-gray-50 rounded-lg grid grid-cols-1 md:grid-cols-2 gap-4">
													<div>
														<label
															htmlFor={`quantity-${product.id}`}
															className="block text-sm font-medium text-gray-700 mb-1"
														>
															수량 <span className="text-red-500">*</span>
														</label>
														<input
															type="number"
															id={`quantity-${product.id}`}
															value={auctionProduct.amount}
															onChange={(e) => handleQuantityChange(product.id, parseInt(e.target.value, 10))}
															min="1"
															required
															className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
														/>
													</div>
													<div>
														<label
															htmlFor={`price-${product.id}`}
															className="block text-sm font-medium text-gray-700 mb-1"
														>
															입찰 시작가 (원) <span className="text-red-500">*</span>
														</label>
														<input
															type="number"
															id={`price-${product.id}`}
															value={auctionProduct.startPrice}
															onChange={(e) => handleStartPriceChange(product.id, parseInt(e.target.value, 10))}
															min="0"
															required
															className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
														/>
													</div>
												</div>
											)}
										</div>
									);
								})}
							</div>
						)}
					</div>

					{/* 버튼 */}
					<div className="flex justify-end space-x-3 pt-4">
						<button
							type="button"
							onClick={() => navigate("/mypage")}
							className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 transition"
						>
							취소
						</button>
						<button
							type="submit"
							className="px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition"
						>
							라이브 생성하기
						</button>
					</div>
				</form>
			)}
		</div>
	);
};

export default AuctionCreate;