import React, { useState, useEffect } from "react";
import axiosInstance from "../api/axiosInstance";

interface CategoryData {
	categoryId: number;
	categoryName: string;
	superCategoryId?: number;
	superCategoryName?: string;
	grade?: string;
}

interface HierarchicalCategorySelectorProps {
	onCategorySelect: (categoryId: number, grade: string) => void;
}

const HierarchicalCategorySelector: React.FC<HierarchicalCategorySelectorProps> = ({ onCategorySelect }) => {
	const [superCategories, setSuperCategories] = useState<CategoryData[]>([]);
	const [subCategories, setSubCategories] = useState<CategoryData[]>([]);
	const [availableGrades, setAvailableGrades] = useState<string[]>([]);
	const [selectedSuperCategory, setSelectedSuperCategory] = useState<CategoryData | null>(null);
	const [selectedSubCategory, setSelectedSubCategory] = useState<CategoryData | null>(null);
	const [selectedGrade, setSelectedGrade] = useState<string>("");
	// const [loadingSuper, setLoadingSuper] = useState(false);
	const [loadingSub, setLoadingSub] = useState(false);
	const [loadingGrades, setLoadingGrades] = useState(false);

	// 캐시 상태 추가
	const [cachedSubCategories, setCachedSubCategories] = useState<Map<number, CategoryData[]>>(new Map());
	const [cachedGrades, setCachedGrades] = useState<Map<number, string[]>>(new Map());

	// 모든 상위 카테고리 로드 (데이터 유무 관계없이)
	const loadSuperCategories = async () => {
		// 이미 로드된 데이터가 있으면 스킵
		if (superCategories.length > 0) {
			console.log("상위카테고리 이미 로드됨 - 캐시 사용");
			return;
		}

		// setLoadingSuper(true);

		// 성능 측정 시작
		const startTime = performance.now();
		console.time("상위카테고리 로딩");

		try {
			const response = await axiosInstance.get("/price/super-categories");
			if (response.data.success) {
				setSuperCategories(response.data.data);
				// localStorage에 캐시 저장 (1시간 유효)
				localStorage.setItem(
					"superCategories",
					JSON.stringify({
						data: response.data.data,
						timestamp: Date.now(),
					})
				);
			}
		} catch (error) {
			console.error("상위 카테고리 로드 실패:", error);
			// 캐시된 데이터가 있으면 사용
			const cached = localStorage.getItem("superCategories");
			if (cached) {
				const { data, timestamp } = JSON.parse(cached);
				const isExpired = Date.now() - timestamp > 3600000; // 1시간
				if (!isExpired) {
					console.log("캐시된 상위카테고리 데이터 사용");
					setSuperCategories(data);
				}
			}
		} finally {
			// setLoadingSuper(false);
			// 성능 측정 종료
			const endTime = performance.now();
			console.timeEnd("상위카테고리 로딩");
			console.log(`상위카테고리 로딩 시간: ${(endTime - startTime).toFixed(2)}ms`);
		}
	};

	// 상위 카테고리 선택 시 하위 카테고리 로드
	const handleSuperCategorySelect = async (category: CategoryData) => {
		// 이미 선택된 카테고리를 다시 클릭하면 선택 해제
		if (selectedSuperCategory?.categoryId === category.categoryId) {
			setSelectedSuperCategory(null);
			setSelectedSubCategory(null);
			setSelectedGrade("");
			setSubCategories([]);
			return;
		}

		setSelectedSuperCategory(category);
		setSelectedSubCategory(null);
		setSelectedGrade("");

		// 성능 측정 시작
		const startTime = performance.now();
		console.time(`하위카테고리 로딩 - ${category.categoryName}`);

		try {
			// 1. 메모리 캐시 확인
			if (cachedSubCategories.has(category.categoryId)) {
				console.log(`메모리 캐시 사용: ${category.categoryName}`);
				setSubCategories(cachedSubCategories.get(category.categoryId)!);
				return;
			}

			// 2. localStorage 캐시 확인
			const cacheKey = `subCategories_${category.categoryId}`;
			const cached = localStorage.getItem(cacheKey);
			if (cached) {
				const { data, timestamp } = JSON.parse(cached);
				const isExpired = Date.now() - timestamp > 1800000; // 30분
				if (!isExpired) {
					console.log(`localStorage 캐시 사용: ${category.categoryName}`);
					setSubCategories(data);
					setCachedSubCategories((prev) => new Map(prev).set(category.categoryId, data));
					return;
				}
			}

			// 3. API 호출
			setLoadingSub(true);
			const response = await axiosInstance.get(`/price/sub-categories-with-data/${category.categoryId}`);
			if (response.data.success) {
				setSubCategories(response.data.data);
				// 메모리 캐시에 저장
				setCachedSubCategories((prev) => new Map(prev).set(category.categoryId, response.data.data));
				// localStorage에 저장 (30분 유효)
				localStorage.setItem(
					cacheKey,
					JSON.stringify({
						data: response.data.data,
						timestamp: Date.now(),
					})
				);
			} else {
				setSubCategories([]);
			}
		} catch (error) {
			console.error("하위 카테고리 로드 실패:", error);
			setSubCategories([]);
		} finally {
			setLoadingSub(false);
			// 성능 측정 종료
			const endTime = performance.now();
			console.timeEnd(`하위카테고리 로딩 - ${category.categoryName}`);
			console.log(`실제 로딩 시간: ${(endTime - startTime).toFixed(2)}ms`);
		}
	};

	// 하위 카테고리 선택 시 등급 로드
	const handleSubCategorySelect = async (category: CategoryData) => {
		setSelectedSubCategory(category);
		setSelectedGrade("");

		// 성능 측정 시작
		const startTime = performance.now();
		console.time(`등급 로딩 - ${category.categoryName}`);

		try {
			// 1. 메모리 캐시 확인
			if (cachedGrades.has(category.categoryId)) {
				console.log(`메모리 캐시 등급 사용: ${category.categoryName}`);
				setAvailableGrades(cachedGrades.get(category.categoryId)!);
				return;
			}

			// 2. localStorage 캐시 확인
			const cacheKey = `grades_${category.categoryId}`;
			const cached = localStorage.getItem(cacheKey);
			if (cached) {
				const { data, timestamp } = JSON.parse(cached);
				const isExpired = Date.now() - timestamp > 1800000; // 30분
				if (!isExpired) {
					console.log(`localStorage 캐시 등급 사용: ${category.categoryName}`);
					setAvailableGrades(data);
					setCachedGrades((prev) => new Map(prev).set(category.categoryId, data));
					return;
				}
			}

			// 3. API 호출
			setLoadingGrades(true);
			const response = await axiosInstance.get(`/price/available-grades/${category.categoryId}`);
			if (response.data.success && response.data.data.length > 0) {
				setAvailableGrades(response.data.data);
				// 메모리 캐시에 저장
				setCachedGrades((prev) => new Map(prev).set(category.categoryId, response.data.data));
				// localStorage에 저장 (30분 유효)
				localStorage.setItem(
					cacheKey,
					JSON.stringify({
						data: response.data.data,
						timestamp: Date.now(),
					})
				);
			} else {
				// 데이터가 없으면 기본 등급들 표시
				setAvailableGrades(["특", "상", "중", "하"]);
				setCachedGrades((prev) => new Map(prev).set(category.categoryId, ["특", "상", "중", "하"]));
				localStorage.setItem(
					cacheKey,
					JSON.stringify({
						data: ["특", "상", "중", "하"],
						timestamp: Date.now(),
					})
				);
			}
		} catch (error) {
			console.error("사용 가능한 등급 조회 실패:", error);
			setAvailableGrades(["특", "상", "중", "하"]);
			setCachedGrades((prev) => new Map(prev).set(category.categoryId, ["특", "상", "중", "하"]));
		} finally {
			setLoadingGrades(false);
			// 성능 측정 종료
			const endTime = performance.now();
			console.timeEnd(`등급 로딩 - ${category.categoryName}`);
			console.log(`실제 등급 로딩 시간: ${(endTime - startTime).toFixed(2)}ms`);
		}
	};

	// 등급 선택 시 차트 데이터 로드
	const handleGradeSelect = (grade: string) => {
		setSelectedGrade(grade);
		if (selectedSubCategory) {
			onCategorySelect(selectedSubCategory.categoryId, grade);
		}
	};

	useEffect(() => {
		// 초기 로드 시 localStorage에서 캐시 확인
		const cached = localStorage.getItem("superCategories");
		if (cached) {
			const { data, timestamp } = JSON.parse(cached);
			const isExpired = Date.now() - timestamp > 3600000; // 1시간
			if (!isExpired) {
				console.log("초기 로드: 캐시된 상위카테고리 데이터 사용");
				setSuperCategories(data);
				return;
			}
		}
		loadSuperCategories();
	}, []);

	useEffect(() => {
		// superCategories가 로드되고 아직 선택된 카테고리가 없을 때 첫 번째 카테고리 자동 선택
		if (superCategories.length > 0 && !selectedSuperCategory) {
			console.log("첫 번째 상위 카테고리 자동 선택:", superCategories[0].categoryName);
			handleSuperCategorySelect(superCategories[0]);
		}
	}, [superCategories, selectedSuperCategory]);

	return (
		<div className="p-6 md:p-8 max-w-6xl mx-auto">
			<h1 className="text-2xl font-bold text-gray-800 text-center mt-5">낙찰가 차트</h1>
			<p className="text-gray-800 text-center my-2">카테고리와 등급을 선택하여 가격 추이를 확인하세요!</p>

			{/* 상위 카테고리 선택 */}
			<div className="flex flex-wrap gap-4 mt-8 border-b justify-center">
				{/* <button
					className={`pb-2 px-4 h-8 flex items-center justify-center border-b-2 text-sm font-semibold ${
						selectedSuperCategory === null
							? "border-orange-500 text-orange-500"
							: "border-transparent text-gray-500 hover:text-orange-400"
					}`}
					onClick={() => {
						setSelectedSuperCategory(null);
						setSelectedSubCategory(null);
						setSelectedGrade("");
					}}
				>
					전체
				</button> */}

				{superCategories.map((cat) => (
					<button
						key={cat.categoryId}
						className={`pb-2 px-4 h-8 flex items-center justify-center border-b-2 text-sm font-semibold ${
							selectedSuperCategory?.categoryId === cat.categoryId
								? "border-orange-500 text-orange-500"
								: "border-transparent text-gray-500 hover:text-orange-400"
						}`}
						onClick={() => handleSuperCategorySelect(cat)}
					>
						{cat.categoryName}
					</button>
				))}
			</div>

			{/* 하위 카테고리와 등급 선택 */}
			{selectedSuperCategory && (
				<div className="mt-6">
					<div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
						{/* 하위 카테고리 선택 */}
						<div>
							<h3 className="text-lg font-semibold text-gray-800 mb-4 text-center">
								{selectedSuperCategory.categoryName} 품목 선택
							</h3>
							{loadingSub ? (
								<div className="flex flex-col items-center justify-center space-y-2">
									<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
									<p className="text-sm text-gray-500">품목 데이터 로딩 중...</p>
								</div>
							) : (
								<div className="grid grid-cols-2 gap-2 max-h-60 overflow-y-auto">
									{subCategories.map((category) => (
										<button
											key={category.categoryId}
											className={`p-2 rounded-lg font-medium transition-all duration-200 text-sm ${
												selectedSubCategory?.categoryId === category.categoryId
													? "bg-orange-500 text-white shadow-lg"
													: "bg-white hover:bg-orange-50 text-gray-700 hover:shadow-md border border-gray-200"
											}`}
											onClick={() => handleSubCategorySelect(category)}
										>
											{category.categoryName}
										</button>
									))}
								</div>
							)}
						</div>

						{/* 등급 선택 */}
						<div>
							<h3 className="text-lg font-semibold text-gray-800 mb-4 text-center">등급 선택</h3>
							{loadingGrades ? (
								<div className="flex flex-col items-center justify-center space-y-2">
									<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
									<p className="text-sm text-gray-500">등급 데이터 로딩 중...</p>
								</div>
							) : selectedSubCategory ? (
								<div className="grid grid-cols-2 gap-3 max-w-xs mx-auto">
									{availableGrades.map((grade) => (
										<button
											key={grade}
											className={`p-4 rounded-lg font-semibold transition-all duration-200 ${
												selectedGrade === grade
													? "bg-orange-500 text-white shadow-lg"
													: "bg-white hover:bg-orange-50 text-gray-700 hover:shadow-md border border-gray-200"
											}`}
											onClick={() => handleGradeSelect(grade)}
										>
											{grade}
										</button>
									))}
								</div>
							) : (
								<div className="flex items-center justify-center h-32 text-gray-500">품목을 선택해주세요</div>
							)}
						</div>
					</div>
				</div>
			)}

			{/* 선택된 정보 표시 */}
			{(selectedSuperCategory || selectedSubCategory || selectedGrade) && (
				<div className="mt-8 p-4 bg-orange-50 rounded-lg border border-orange-200">
					<div className="flex items-center justify-center space-x-2 text-gray-700">
						{selectedSuperCategory && (
							<span className="bg-white px-3 py-1 rounded-full text-sm font-medium border border-orange-200">
								{selectedSuperCategory.categoryName}
							</span>
						)}
						{selectedSubCategory && (
							<>
								<span className="text-orange-400">→</span>
								<span className="bg-white px-3 py-1 rounded-full text-sm font-medium border border-orange-200">
									{selectedSubCategory.categoryName}
								</span>
							</>
						)}
						{selectedGrade && (
							<>
								<span className="text-orange-400">→</span>
								<span className="bg-white px-3 py-1 rounded-full text-sm font-medium border border-orange-200">
									{selectedGrade}
								</span>
							</>
						)}
					</div>
				</div>
			)}
		</div>
	);
};

export default HierarchicalCategorySelector;
