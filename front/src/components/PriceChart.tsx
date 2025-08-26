import React, { useState } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { loadChartDataById, type ChartData } from "../data/chartDataManager";
import HierarchicalCategorySelector from "./HierarchicalCategorySelector";

const PriceChart: React.FC = () => {
	const [chartData, setChartData] = useState<ChartData | null>(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);

	const handleCategorySelect = async (categoryId: number, grade: string) => {
		setLoading(true);
		setError(null);
		try {
			const data = await loadChartDataById(categoryId, grade);
			if (data) {
				setChartData(data);
			} else {
				setError("차트 데이터를 불러올 수 없습니다.");
			}
		} catch (err) {
			console.error("차트 데이터 로드 실패:", err);
			setError("차트 데이터 로드에 실패했습니다.");
		} finally {
			setLoading(false);
		}
	};

	const prepareChartData = () => {
		if (!chartData) return [];
		const dataMap = new Map<string, { actual: number | null; prediction: number | null }>();
		chartData.actual.dates.forEach((date, index) => {
			const dateStr = new Date(date).toLocaleDateString("ko-KR");
			dataMap.set(dateStr, { actual: chartData.actual.prices[index], prediction: null });
		});
		chartData.prediction.dates.forEach((date, index) => {
			const dateStr = new Date(date).toLocaleDateString("ko-KR");
			const existing = dataMap.get(dateStr) || { actual: null, prediction: null };
			dataMap.set(dateStr, { ...existing, prediction: chartData.prediction.prices[index] });
		});
		return Array.from(dataMap.entries())
			.map(([date, prices]) => ({ date, actual: prices.actual, prediction: prices.prediction }))
			.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
	};

	const chartDataPoints = prepareChartData();

	return (
		<div className="bg-white">
			<div className="w-full max-w-6xl mx-auto px-6">
				{/* 검색 UI */}
				<HierarchicalCategorySelector onCategorySelect={handleCategorySelect} />

				{/* 차트 영역 - 고정 위치 */}
				<div className="p-6 md:p-8">
					<div
						className="relative w-full p-6 mx-auto mb-20 sm:mt-4 md:mt-6 bg-white rounded-2xl shadow-xs border border-gray-100 overflow-hidden"
						style={{ minHeight: "600px" }}
					>
						{loading && (
							<div className="absolute p-6 inset-0 flex items-center justify-center bg-white bg-opacity-90 z-10">
								<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
								<p className="ml-4 text-lg text-gray-600">차트 데이터를 불러오는 중...</p>
							</div>
						)}
						{error && (
							<div className="absolute p-6 inset-0 flex items-center justify-center bg-white bg-opacity-90 z-10">
								<div className="text-center">
									<p className="text-red-600 text-lg">{error}</p>
								</div>
							</div>
						)}
						{chartData && !loading && !error && (
							<div className="p-6">
								<div className="mb-6">
									<h2 className="text-2xl font-bold text-gray-800 mb-3">
										{chartData.item} ({chartData.grade}) 가격 추이
									</h2>
									<div className="flex flex-wrap gap-6 text-sm text-gray-600">
										<div className="flex items-center space-x-2">
											<div className="w-3 h-3 bg-orange-500 rounded-full"></div>
											<span>
												최근 실제 가격:{" "}
												<span className="font-semibold">{chartData.last_actual_price?.toLocaleString()}원/kg</span>
											</span>
										</div>
										<div className="flex items-center space-x-2">
											<div className="w-3 h-3 bg-blue-500 rounded-full"></div>
											<span>
												마지막 업데이트: <span className="font-semibold">{chartData.last_actual_date}</span>
											</span>
										</div>
									</div>
								</div>
								<div className="h-[600px] w-full" style={{ position: "relative" }}>
									<ResponsiveContainer width="100%" height="100%">
										<LineChart data={chartDataPoints} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
											<CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
											<XAxis
												dataKey="date"
												angle={-45}
												textAnchor="end"
												height={100}
												interval={Math.ceil(chartDataPoints.length / 15)}
												tick={{ fontSize: 12 }}
												tickLine={false}
											/>
											<YAxis
												domain={["dataMin - 1000", "dataMax + 1000"]}
												tickFormatter={(value) => `${value.toLocaleString()}원`}
												tick={{ fontSize: 12 }}
												tickLine={false}
												axisLine={false}
											/>
											<Tooltip
												formatter={(value: any, name: string) => [
													`${value?.toLocaleString()}원`,
													name === "actual" ? "실제 가격" : "예측 가격",
												]}
												labelFormatter={(label) => `날짜: ${label}`}
												contentStyle={{
													backgroundColor: "white",
													border: "1px solid #e5e7eb",
													borderRadius: "8px",
													boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
												}}
											/>
											<Legend
												wrapperStyle={{ paddingTop: "20px" }}
												formatter={(value) => (value === "actual" ? "실제 가격" : "예측 가격")}
											/>
											<Line
												type="monotone"
												dataKey="actual"
												stroke="#f97316"
												strokeWidth={3}
												dot={false}
												name="actual"
												connectNulls={true}
											/>
											<Line
												type="monotone"
												dataKey="prediction"
												stroke="#3b82f6"
												strokeWidth={3}
												strokeDasharray="8 8"
												dot={false}
												name="prediction"
												connectNulls={true}
											/>
										</LineChart>
									</ResponsiveContainer>
								</div>
							</div>
						)}
						{!chartData && !loading && !error && (
							<div className="absolute p-6 inset-0 flex flex-col items-center justify-center bg-white">
								<p className="text-gray-500 text-xl mb-2">차트 데이터가 없습니다</p>
								<p className="text-gray-400">카테고리와 등급을 선택하여 차트를 확인하세요</p>
							</div>
						)}
					</div>
				</div>
			</div>
		</div>
	);
};

export default PriceChart;
