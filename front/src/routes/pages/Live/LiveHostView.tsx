import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import LiveAuctionCard from "../../../components/Live/LiveAuctionCard";
import BidList from "../../../components/Live/BidList";
import AuctionList from "../../../components/Live/AuctionList";
import HostGuideModal from "../../../components/Live/HostGuideModal";
import axiosInstance from "../../../api/axiosInstance";
import { useWebRTC } from "../../../hooks/useWebRTC";
import freshCheckApiInstance from "../../../api/freshCheckApiInstance";
import FreshCheckLoadingModal from "../../../components/Live/FreshCheckLoadingModal";

// 타입 정의
interface Product {
	id: number;
	name: string;
	origin: string;
	weight: string;
	reprImgSrc: string;
	description: string;
	grade: string;
	createdAt: string;
	userId: number;
	username: string;
	categoryId: number;
	categoryName: string;
}

interface Auction {
	id: number;
	startPrice: number;
	amount: number;
	status: string;
	createdAt: string;
	product: Product;
}

interface Seller {
	sellerId: number;
	nickname: string;
}

interface LiveData {
	id: number;
	seller: Seller;
	title: string;
	startDate: string;
	endDate: string;
	auctions: Auction[];
}

interface ApiResponse {
	success: boolean;
	message: string;
	data: LiveData;
}

interface BidListItem {
	userNickName: string;
	bidPrice: number;
	bidTime?: string;
}

const LiveHostView: React.FC = () => {
	const { id } = useParams();
	const roomId = Number(id);
	const navigate = useNavigate();

	const [liveData, setLiveData] = useState<LiveData | null>(null);
	const [loading, setLoading] = useState<boolean>(true);
	const [freshCheckLoading, setFreshCheckLoading] = useState<boolean>(false);
	const [freshNess, setFreshNess] = useState<number | null>(null);
	const [error, setError] = useState<string | null>(null);
	const [bids, setBids] = useState<BidListItem[]>([]);
	// 안내 모달 상태 추가
	const [showGuideModal, setShowGuideModal] = useState<boolean>(false);
	// 신선도 체크 요청 메시지 상태 추가
	const [freshNessRequestMessage, setFreshNessRequestMessage] = useState<string | null>(null);

	const currentAuction = liveData?.auctions.find((a) => a.status === "IN_PROGRESS") || liveData?.auctions[0];
	const nextAuction = liveData?.auctions.find((a) => a.status === "SCHEDULED");

	// 최신 입찰가 계산
	const latestBidPrice = bids.length > 0 ? Math.max(...bids.map((b) => b.bidPrice)) : currentAuction?.startPrice ?? 0;

	const { localVideoRef, remoteVideoRef, sendMessage, disconnect } = useWebRTC(roomId, "host", (msg) => {
		console.log("💰 WebSocket 메시지 수신:", msg);
		console.log("🔍 메시지 타입:", msg.type);
		console.log("🔍 전체 메시지 구조:", JSON.stringify(msg, null, 2));
		if (msg.type === "bidStatusUpdate") {
			if (Array.isArray(msg.bidList)) {
				console.log("✅ bidList 직접 사용:", msg.bidList);

				// 중복 입찰 필터링 (같은 사용자의 같은 금액 입찰은 하나만 유지)
				const uniqueBids = msg.bidList.filter(
					(bid, index, self) =>
						index === self.findIndex((b) => b.userNickName === bid.userNickName && b.bidPrice === bid.bidPrice)
				);

				console.log("🔍 중복 필터링 후 입찰 수:", uniqueBids.length, "원본:", msg.bidList.length);
				setBids(uniqueBids);
			} else {
				console.warn("⚠️ bidList가 없습니다:", msg);
			}
		} else if (msg.type === "submitBidResult") {
			console.log("🎯 [HOST] 입찰 결과 수신:", msg);
			if (msg.success) {
				console.log("✅ [HOST] 입찰 성공");
			}
		} else if (msg.type === "startAuctionResult") {
			console.log("🎯 [HOST] 경매 시작 결과 수신:", msg);
			if (msg.success) {
				console.log("✅ [HOST] 경매 시작 성공 - 알림 표시");
				alert("경매가 시작되었습니다!");
				// 라이브 데이터 새로고침
				refreshLiveData();
			} else {
				console.log("❌ [HOST] 경매 시작 실패:", msg.message);
				alert("경매 시작에 실패했습니다: " + msg.message);
			}
		} else if (msg.type === "stopAuctionResult") {
			console.log("🎯 [HOST] 경매 종료 결과 수신:", msg);
			if (msg.success) {
				console.log("✅ [HOST] 경매 종료 성공 - 알림 표시");
				alert(msg.message || "경매가 종료되었습니다!");
				// 라이브 데이터 새로고침
				refreshLiveData();
			} else {
				console.log("❌ [HOST] 경매 종료 실패:", msg.message);
				alert("경매 종료에 실패했습니다: " + msg.message);
			}
		} else if (msg.type === "freshNessRequest") {
			console.log("🍃 [HOST] 신선도 체크 요청 수신:", msg);
			setFreshNessRequestMessage("신선도 체크 요청이 수신되었습니다.");
		} else {
			console.log("📝 [HOST] 기타 메시지:", msg.type);
		}
	});

	const refreshLiveData = () => {
		setLoading(true);
		axiosInstance
			.get<ApiResponse>(`/auction/live/${roomId}`)
			.then((response) => {
				setLiveData(response.data.data);
				setLoading(false);
			})
			.catch((err) => {
				console.error("❌ 라이브 정보 새로고침 실패:", err);
				setLoading(false);
			});
	};

	useEffect(() => {
		setLoading(true);
		axiosInstance
			.get<ApiResponse>(`/auction/live/${roomId}`)
			.then((response) => {
				setLiveData(response.data.data);
				setLoading(false);
				// 라이브 정보 로드 완료 후 안내 모달 표시
				setShowGuideModal(true);
			})
			.catch((err) => {
				console.error("❌ 라이브 정보 로딩 실패:", err);
				setError("라이브 정보를 불러오는데 실패했습니다.");
				setLoading(false);
			});
	}, [roomId]);

	// 신선도 체크 요청 메시지 자동 사라짐 로직
	useEffect(() => {
		if (freshNessRequestMessage) {
			const timer = setTimeout(() => {
				setFreshNessRequestMessage(null);
			}, 3000);

			return () => clearTimeout(timer);
		}
	}, [freshNessRequestMessage]);

	const leaveRoom = () => {
		disconnect();
		navigate("/live");
	};

	// 경매 상태에 따른 버튼 텍스트와 기능 결정
	const getAuctionButtonInfo = () => {
		if (!liveData) return { text: "로딩 중...", action: () => {}, disabled: true };

		const hasInProgressAuction = liveData.auctions.some((a) => a.status === "IN_PROGRESS");
		const hasScheduledAuction = liveData.auctions.some((a) => a.status === "SCHEDULED");
		const hasCompletedAuction = liveData.auctions.some((a) => a.status === "COMPLETED");

		if (hasInProgressAuction) {
			// 진행 중인 경매가 있으면 "낙찰 확정" 버튼
			return {
				text: "낙찰 확정",
				action: () => confirmAuction(),
				disabled: false,
				className: "bg-red-500 hover:bg-red-600",
			};
		} else if (hasScheduledAuction) {
			// 예정된 경매가 있으면 "경매 시작" 버튼
			return {
				text: "경매 시작",
				action: () => startAuction(),
				disabled: false,
				className: "bg-green-500 hover:bg-green-600",
			};
		} else if (hasCompletedAuction) {
			// 완료된 경매가 있고 다음 경매가 있으면 "다음 경매" 버튼
			return {
				text: "다음 경매",
				action: () => moveToNextAuction(),
				disabled: !hasScheduledAuction,
				className: "bg-blue-500 hover:bg-blue-600",
			};
		} else {
			// 모든 경매가 완료된 경우
			return {
				text: "모든 경매 완료",
				action: () => {},
				disabled: true,
				className: "bg-gray-400 cursor-not-allowed",
			};
		}
	};

	const startAuction = () => {
		if (!liveData) return;
		const auctionToStart = liveData.auctions.find((a) => a.status === "SCHEDULED");
		if (!auctionToStart) {
			alert("시작할 수 있는 경매가 없습니다.");
			return;
		}
		sendMessage({
			type: "startAuction",
			roomId: liveData.id,
			auctionId: auctionToStart.id,
			sellerId: liveData.seller.sellerId,
		});
	};

	const confirmAuction = () => {
		if (!liveData || !currentAuction) return;

		const message = {
			type: "stopAuction",
			roomId: liveData.id,
			sellerId: liveData.seller.sellerId,
			auctionId: currentAuction.id,
		};

		console.log("🎯 경매 종료 메시지 전송:", message);
		sendMessage(message);
	};

	const moveToNextAuction = () => {
		if (!liveData || !nextAuction) {
			alert("다음 경매가 없습니다.");
			return;
		}
		// 다음 경매를 현재 경매로 설정하는 로직
		// 실제로는 서버에서 경매 상태를 업데이트해야 함
		console.log("다음 경매로 이동:", nextAuction.id);
		// 여기서는 단순히 페이지를 새로고침하여 최신 상태를 가져옴
		window.location.reload();
	};

	const buttonInfo = getAuctionButtonInfo();

	const freshCheck = () => {
		if (!liveData || !currentAuction) return;
		setFreshNess(null);
		// 신선도 체크 요청 메시지 숨김
		setFreshNessRequestMessage(null);
		const canvas = document.createElement("canvas");
		if (!localVideoRef.current) return;
		canvas.width = localVideoRef.current.videoWidth;
		canvas.height = localVideoRef.current.videoHeight;

		const message = {
			type: "freshCheck",
			roomId: liveData.id,
			freshNessResult: -1,
		};

		const ctx = canvas.getContext("2d");
		if (!ctx) return;

		ctx.drawImage(localVideoRef.current, 0, 0, canvas.width, canvas.height);

		// Blob 형태로 변환 (image/jpeg or image/png 가능)
		canvas.toBlob(async (blob) => {
			if (!blob) return;

			const formData = new FormData();
			formData.append("image", blob, "capture.jpg");

			try {
				setFreshCheckLoading(true);
				const response = await freshCheckApiInstance.post("/classify", formData);
				console.log("서버 응답:", response.data);

				setFreshCheckLoading(false);
				if (response.data.freshness === "The item is VERY FRESH!") {
					console.log("very fresh");
					setFreshNess(2);
					message.freshNessResult = 2;
				} else if (response.data.freshness === "The item is FRESH") {
					console.log("fresh");
					setFreshNess(1);
					message.freshNessResult = 1;
				} else if (response.data.freshness === "The item is NOT FRESH") {
					setFreshNess(0);
					message.freshNessResult = 0;
				} else {
					setFreshNess(-1); //신선도 파악 불가!
					message.freshNessResult = -1;
				}
				console.log("🎯 신선도 체크 완료 메시지 전송:", message);
				sendMessage(message);
			} catch (error) {
				setFreshCheckLoading(false);
				console.error("업로드 실패:", error);
			}
		}, "image/jpeg");
	};

	return (
		<div className="flex p-6 gap-6 bg-slate-50 min-h-screen">
			<div style={{ flex: 2 }}>
				{liveData && (
					<div className="text-gray-800 py-5 font-bold text-3xl text-left mb-5 border-b-2 border-gray-200">
						{liveData.title}
					</div>
				)}
				<div style={{ position: "relative", marginBottom: "24px" }}>
					<video
						ref={localVideoRef} // 내(Host) 화면
						autoPlay
						playsInline
						muted
						style={{
							width: "100%",
							height: "450px",
							borderRadius: "16px",
							backgroundColor: "#000",
							objectFit: "cover",
							boxShadow: "0 10px 25px rgba(0, 0, 0, 0.1)",
						}}
						onLoadedMetadata={() => console.log("✅ 호스트 로컬 비디오 메타데이터 로드됨")}
						onCanPlay={() => console.log("✅ 호스트 로컬 비디오 재생 가능")}
						onError={(e) => console.error("❌ 호스트 로컬 비디오 에러:", e)}
					/>
					<div
						style={{
							position: "absolute",
							bottom: 16,
							left: 16,
							background: "rgba(239, 68, 68, 0.9)",
							color: "white",
							padding: "8px 16px",
							borderRadius: "12px",
							fontWeight: "600",
							fontSize: "14px",
							backdropFilter: "blur(10px)",
						}}
					>
						내 화면 (호스트)
					</div>
					<button
						onClick={freshCheck}
						style={{
							position: "absolute",
							top: 16,
							left: 16,
							background: "rgba(34, 197, 94, 0.9)",
							color: "white",
							padding: "8px 16px",
							borderRadius: "12px",
							fontWeight: "600",
							cursor: "pointer",
							fontSize: "14px",
							border: "none",
							backdropFilter: "blur(10px)",
							transition: "all 0.2s ease",
						}}
					>
						신선도 파악
					</button>
					<div
						style={{
							position: "absolute",
							top: 16,
							left: 140,
							background: "rgba(255, 255, 255, 0.95)",
							padding: "8px 16px",
							borderRadius: "12px",
							fontWeight: "600",
							fontSize: "14px",
							backdropFilter: "blur(10px)",
							boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
						}}
					>
						{freshNess === 2 ? (
							<p style={{ color: "#3b82f6", margin: 0 }}>신선도 : 상</p>
						) : freshNess === 1 ? (
							<p style={{ color: "#10b981", margin: 0 }}>신선도 : 중</p>
						) : freshNess === 0 ? (
							<p style={{ color: "#ef4444", margin: 0 }}>신선도 : 하</p>
						) : freshNess === -1 ? (
							<p style={{ color: "#6b7280", margin: 0 }}>신선도 파악 불가</p>
						) : (
							<p style={{ margin: 0 }}></p>
						)}
					</div>
					{freshNessRequestMessage && (
						<div
							style={{
								position: "absolute",
								top: 16,
								left: 300,
								background: "rgba(34, 197, 94, 0.9)",
								color: "white",
								padding: "8px 16px",
								borderRadius: "12px",
								fontWeight: "600",
								fontSize: "14px",
								backdropFilter: "blur(10px)",
								boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
								transition: "opacity 0.3s ease",
								opacity: freshNessRequestMessage ? 1 : 0,
							}}
						>
							<p style={{ margin: 0 }}>{freshNessRequestMessage}</p>
						</div>
					)}
					<div></div>
					{freshCheckLoading ? <FreshCheckLoadingModal /> : <></>}
				</div>

				<div style={{ padding: "16px 0" }}>
					{loading ? (
						<p
							style={{
								fontSize: "16px",
								color: "#6b7280",
								textAlign: "center",
								padding: "20px",
							}}
						>
							라이브 정보 로딩 중...
						</p>
					) : error ? (
						<p
							style={{
								color: "#ef4444",
								fontSize: "16px",
								textAlign: "center",
								padding: "20px",
							}}
						>
							{error}
						</p>
					) : liveData ? (
						<>
							{/* 현재 경매 */}
							{currentAuction && (
								<>
									<div
										style={{
											marginBottom: "16px",
											padding: "12px 16px",
											borderRadius: "12px",
											backgroundColor: "#f3f4f6",
											border: "1px solid #e5e7eb",
										}}
									>
										<div
											style={{
												display: "flex",
												justifyContent: "space-between",
												alignItems: "center",
											}}
										>
											<p
												style={{
													fontSize: "15px",
													color: "#374151",
													margin: "0",
													fontWeight: "500",
												}}
											>
												경매 상태:{" "}
												<strong style={{ color: "#059669" }}>
													{currentAuction.status === "IN_PROGRESS"
														? "진행 중"
														: currentAuction.status === "COMPLETED"
														? "완료"
														: "대기 중"}
												</strong>
											</p>
											<p
												style={{
													fontSize: "15px",
													color: "#92400e",
													margin: "0",
													fontWeight: "600",
												}}
											>
												최신 입찰가: <strong style={{ fontSize: "16px" }}>{latestBidPrice.toLocaleString()}원</strong>
											</p>
										</div>
									</div>
									<div
										style={{
											backgroundColor: "#ffffff",
											padding: "20px",
											borderRadius: "16px",
											boxShadow: "0 8px 25px rgba(0, 0, 0, 0.12)",
											border: "2px solid #e5e7eb",
											marginBottom: "8px",
										}}
									>
										<h3
											style={{
												fontSize: "22px",
												fontWeight: "700",
												marginBottom: "16px",
												color: "#1f2937",
											}}
										>
											현재 경매
										</h3>
										<LiveAuctionCard
											productName={currentAuction.product.name}
											weight={Number(currentAuction.product.weight)}
											amount={currentAuction.amount}
											grade={currentAuction.product.grade}
											origin={currentAuction.product.origin}
											startPrice={currentAuction.startPrice}
										/>
									</div>
								</>
							)}

							{/* 다음 경매 */}
							{nextAuction ? (
								<>
									<div
										style={{
											backgroundColor: "#f9fafb",
											padding: "16px",
											borderRadius: "12px",
											boxShadow: "0 2px 8px rgba(0, 0, 0, 0.06)",
											border: "1px solid #e5e7eb",
											marginTop: "24px",
										}}
									>
										<h3
											style={{
												fontSize: "18px",
												fontWeight: "600",
												marginBottom: "12px",
												color: "#6b7280",
											}}
										>
											다음 경매
										</h3>
										<LiveAuctionCard
											productName={nextAuction.product.name}
											weight={Number(nextAuction.product.weight)}
											amount={nextAuction.amount}
											grade={nextAuction.product.grade}
											origin={nextAuction.product.origin}
											startPrice={nextAuction.startPrice}
										/>
									</div>
								</>
							) : (
								<div
									style={{
										marginTop: "24px",
										padding: "16px",
										backgroundColor: "#f3f4f6",
										borderRadius: "12px",
										border: "1px solid #e5e7eb",
										textAlign: "center",
										boxShadow: "0 2px 8px rgba(0, 0, 0, 0.06)",
									}}
								>
									<p
										style={{
											fontSize: "16px",
											color: "#6b7280",
											margin: "0",
											fontWeight: "500",
										}}
									>
										마지막 경매입니다
									</p>
								</div>
							)}
						</>
					) : (
						<p
							style={{
								fontSize: "16px",
								color: "#6b7280",
								textAlign: "center",
								padding: "20px",
							}}
						>
							라이브 정보가 없습니다.
						</p>
					)}
				</div>

				<button
					onClick={leaveRoom}
					style={{
						backgroundColor: "#ef4444",
						color: "white",
						width: "100%",
						padding: "16px",
						borderRadius: "12px",
						marginTop: "24px",
						fontSize: "16px",
						fontWeight: "600",
						border: "none",
						cursor: "pointer",
						transition: "all 0.2s ease",
						boxShadow: "0 4px 12px rgba(239, 68, 68, 0.3)",
					}}
				>
					라이브 종료하기
				</button>
			</div>

			<div
				style={{
					flex: 1,
					display: "flex",
					flexDirection: "column",
					gap: "24px",
					backgroundColor: "#ffffff",
					padding: "24px",
					borderRadius: "20px",
					boxShadow: "0 8px 32px rgba(0, 0, 0, 0.08)",
					border: "1px solid #e5e7eb",
					marginLeft: "8px",
				}}
			>
				<BidList bids={bids} />

				<div
					style={{
						backgroundColor: "#f9fafb",
						padding: "20px",
						borderRadius: "16px",
						border: "1px solid #e5e7eb",
					}}
				>
					<h4
						style={{
							fontSize: "18px",
							fontWeight: "700",
							marginBottom: "16px",
							color: "#1f2937",
						}}
					>
						경매 리스트
					</h4>
					{liveData && <AuctionList auctions={liveData.auctions} />}

					<div style={{ marginTop: "20px" }}>
						<button
							onClick={buttonInfo.action}
							disabled={buttonInfo.disabled}
							className={`w-full text-white py-3 rounded-xl text-base font-semibold transition ${
								buttonInfo.disabled ? "opacity-50 cursor-not-allowed" : ""
							} ${buttonInfo.className}`}
							style={{
								boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
								border: "none",
							}}
						>
							{buttonInfo.text}
						</button>
					</div>
				</div>

				<div
					style={{
						flex: 1,
						backgroundColor: "#f9fafb",
						padding: "20px",
						borderRadius: "16px",
						border: "1px solid #e5e7eb",
					}}
				>
					<h3
						style={{
							fontSize: "18px",
							fontWeight: "700",
							marginBottom: "16px",
							color: "#1f2937",
						}}
					>
						참가자 화면
					</h3>
					<div style={{ position: "relative" }}>
						<video
							ref={remoteVideoRef}
							autoPlay
							playsInline
							style={{
								width: "100%",
								height: "200px",
								border: "2px solid #3b82f6",
								borderRadius: "12px",
								boxShadow: "0 4px 12px rgba(59, 130, 246, 0.2)",
								objectFit: "cover",
								objectPosition: "center",
							}}
							onLoadedMetadata={() => console.log("✅ 호스트 원격 비디오 메타데이터 로드됨")}
							onCanPlay={() => console.log("✅ 호스트 원격 비디오 재생 가능")}
							onPlay={() => console.log("🎬 호스트 원격 비디오 재생 시작됨")}
							onError={(e) => console.error("❌ 호스트 원격 비디오 오류:", e)}
						/>
					</div>
				</div>
			</div>

			{/* 안내 모달 */}
			<HostGuideModal isOpen={showGuideModal} onClose={() => setShowGuideModal(false)} />
		</div>
	);
};

export default LiveHostView;
