import React, { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import LiveAuctionCard from "../../../components/Live/LiveAuctionCard";
import BidList from "../../../components/Live/BidList";
import AuctionList from "../../../components/Live/AuctionList";
import BidModal from "../../../components/Live/BidModal";
import ParticipantGuideModal from "../../../components/Live/ParticipantGuideModal";
import GestureGuideModal from "../../../components/Live/GestureGuideModal";
import axiosInstance from "../../../api/axiosInstance";
import { useWebRTC } from "../../../hooks/useWebRTC";
import {
  useGestureModel,
  type GestureResult,
} from "../../../hooks/useGestureModel";
import { HAND_CONNECTIONS, Hands, type Results } from "@mediapipe/hands";
import { drawConnectors, drawLandmarks } from "@mediapipe/drawing_utils";
import { Camera } from "@mediapipe/camera_utils";

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

const LiveParticipantView: React.FC = () => {
  const { id } = useParams();
  const roomId = Number(id);
  const navigate = useNavigate();

  const [liveData, setLiveData] = useState<LiveData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [bids, setBids] = useState<BidListItem[]>([]);
  const [showModal, setShowModal] = useState<boolean>(false);
  // AI 입찰 확인 모달 상태
  const [showAIBidModal, setShowAIBidModal] = useState<boolean>(false);
  const [aiBidAmount, setAiBidAmount] = useState<number>(0);
  // 안내 모달 상태
  const [showGuideModal, setShowGuideModal] = useState<boolean>(false);
  // 제스처 가이드 모달 상태
  const [showGestureGuideModal, setShowGestureGuideModal] =
    useState<boolean>(false);
  // 신선도 상태
  const [freshNess, setFreshNess] = useState<string | null>(null);

  // AI 제스처 입찰 성공 여부 추적
  const aiBidSuccessRef = useRef<boolean>(false);
  const aiBidAmountRef = useRef<number>(0); // AI 입찰 금액 저장
  
  // AI 제스처 입찰 재시도 관련 상태
  const [pendingAIBid, setPendingAIBid] = useState<{amount: number, timestamp: number} | null>(null);

  const { localVideoRef, remoteVideoRef, sendMessage, disconnect } = useWebRTC(
    roomId,
    "participant",
    (msg) => {
      console.log("💰 WebSocket 메시지 수신:", msg);
      console.log("🔍 메시지 타입:", msg.type);
      console.log("🔍 전체 메시지 구조:", JSON.stringify(msg, null, 2));
      if (msg.type === "bidStatusUpdate") {
        console.log("📊 bidStatusUpdate 수신 - 전체 메시지:", msg);
        if (Array.isArray(msg.bidList)) {
          console.log("✅ bidList 직접 사용:", msg.bidList);
          console.log("📈 currentHighestPrice:", msg.currentHighestPrice);
          console.log("🏷️ auctionId:", msg.auctionId);
          console.log("📊 status:", msg.status);

          // 중복 입찰 필터링 (같은 사용자의 같은 금액 입찰은 하나만 유지)
          const uniqueBids = msg.bidList.filter(
            (bid, index, self) =>
              index ===
              self.findIndex(
                (b) =>
                  b.userNickName === bid.userNickName &&
                  b.bidPrice === bid.bidPrice
              )
          );

          console.log(
            "🔍 중복 필터링 후 입찰 수:",
            uniqueBids.length,
            "원본:",
            msg.bidList.length
          );
          console.log("🔍 필터링된 입찰 목록:", uniqueBids);
          setBids(uniqueBids);

          // bidStatusUpdate 수신 시 liveData의 경매 상태 업데이트
          console.log("🔍 bidStatusUpdate 상태 업데이트 조건 체크:");
          console.log("  - liveData 존재:", !!liveData);
          console.log("  - msg.auctionId:", msg.auctionId);
          console.log("  - msg.status:", msg.status);
          console.log("  - 조건 만족:", !!(liveData && msg.auctionId && msg.status));
          
          if (liveData && msg.auctionId && msg.status) {
            console.log("🔄 bidStatusUpdate로 인한 liveData 상태 동기화");
            console.log("🔍 업데이트할 auctionId:", msg.auctionId);
            console.log("🔍 새로운 status:", msg.status);
            console.log("🔍 현재 liveData.auctions:", liveData.auctions);
            
            const updatedAuctions = liveData.auctions.map((auction) => {
							if (auction.id === msg.auctionId) {
								console.log(`🔄 경매 ${auction.id} 상태 변경: ${auction.status} → ${msg.status}`);
								return {
									...auction,
									status: msg.status || auction.status, // undefined인 경우 기존 값 유지
								};
							}
							return auction;
						});
            
            setLiveData({ ...liveData, auctions: updatedAuctions });
            console.log("✅ liveData 업데이트 완료:", updatedAuctions);
          } else {
            console.log("❌ bidStatusUpdate 상태 업데이트 조건 불만족");
            console.log("  - liveData:", liveData);
            console.log("  - msg.auctionId:", msg.auctionId);
            console.log("  - msg.status:", msg.status);
            
                         // liveData가 null이거나 조건이 불만족되면 강제로 라이브 데이터를 새로고침
             console.log("🔄 bidStatusUpdate로 인한 라이브 데이터 강제 새로고침");
             refreshLiveDataSilently();
          }
        } else {
          console.warn("⚠️ bidList가 없습니다:", msg);
        }
      } else if (msg.type === "submitBidResult") {
        console.log("🎯 [PARTICIPANT] 입찰 결과 수신:", msg);
        if (msg.success) {
          console.log("✅ [PARTICIPANT] 입찰 성공");
          console.log(
            "🎉 입찰 성공 후 현재 시간:",
            new Date().toLocaleTimeString()
          );
          // AI 제스처 입찰 성공 시 알림
          if (aiBidSuccessRef.current && aiBidAmountRef.current > 0) {
            alert(
              `수신호 입찰 성공! 🎯\n입찰가: ${aiBidAmountRef.current.toLocaleString()}원`
            );
            aiBidSuccessRef.current = false; // 리셋
            aiBidAmountRef.current = 0; // 리셋
          }
        } else {
          console.log("❌ [PARTICIPANT] 입찰 실패:", msg.message);
          console.log(
            "💥 입찰 실패 후 현재 시간:",
            new Date().toLocaleTimeString()
          );
          alert("입찰에 실패했습니다: " + msg.message);
          aiBidSuccessRef.current = false; // 실패 시 리셋
          aiBidAmountRef.current = 0; // 실패 시 리셋
        }
      } else if (msg.type === "startAuctionResult") {
        console.log("🎯 [PARTICIPANT] 경매 시작 결과 수신:", msg);
        if (msg.success) {
          console.log("✅ [PARTICIPANT] 경매 시작 성공 - 알림 표시");
          alert("경매가 시작되었습니다!");
          // 라이브 데이터 새로고침
          refreshLiveData();
        } else {
          console.log("❌ [PARTICIPANT] 경매 시작 실패:", msg.message);
          alert("경매 시작에 실패했습니다: " + msg.message);
        }
      } else if (msg.type === "stopAuctionResult") {
        console.log("🎯 [PARTICIPANT] 경매 종료 결과 수신:", msg);
        if (msg.success) {
          console.log("✅ [PARTICIPANT] 경매 종료 성공 - 알림 표시");
          alert("경매가 종료되었습니다!");
          // 라이브 데이터 새로고침
          refreshLiveData();
        } else {
          console.log("❌ [PARTICIPANT] 경매 종료 실패:", msg.message);
          alert("경매 종료에 실패했습니다: " + msg.message);
        }
      } else if (msg.type === "winningBidResult") {
        console.log("🎉 [PARTICIPANT] 낙찰 결과 수신:", msg);
        alert(`🎉낙찰되었습니다 (${msg.bidPrice || 0}원)🎉`);
      } else if (msg.type === "freshNessResult") {
        console.log("🍃 [PARTICIPANT] 신선도 결과 수신:", msg);
        setFreshNess(msg.message);
      } else if (msg.type === "leaveParticipant") {
        console.log("🚪 [PARTICIPANT] 라이브 종료 알림 수신");
        alert("라이브가 종료되었습니다!");
        navigate("/");
        window.scrollTo(0, 0);
      } else {
        console.log("📝 [PARTICIPANT] 기타 메시지:", msg.type);
      }
    }
  );

  const refreshLiveData = () => {
    console.log("🔄 refreshLiveData() 호출됨");
    setLoading(true);
    axiosInstance
      .get<ApiResponse>(`/auction/live/${roomId}`)
      .then((response) => {
        console.log("✅ refreshLiveData() API 응답 성공:", response.data.data);
        console.log("🔍 응답의 auctions:", response.data.data.auctions);
        console.log("🔍 IN_PROGRESS 상태인 경매:", response.data.data.auctions.find(a => a.status === "IN_PROGRESS"));
        setLiveData(response.data.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("❌ 라이브 정보 새로고침 실패:", err);
        setLoading(false);
      });
  };

  // AI 제스처 입찰을 위한 조용한 새로고침 (UI에 영향 없음)
  const refreshLiveDataSilently = () => {
    console.log("🔄 refreshLiveDataSilently() 호출됨");
    axiosInstance
      .get<ApiResponse>(`/auction/live/${roomId}`)
      .then((response) => {
        console.log("✅ refreshLiveDataSilently() API 응답 성공:", response.data.data);
        console.log("🔍 응답의 auctions:", response.data.data.auctions);
        console.log("🔍 IN_PROGRESS 상태인 경매:", response.data.data.auctions.find(a => a.status === "IN_PROGRESS"));
        setLiveData(response.data.data);
      })
      .catch((err) => {
        console.error("❌ 조용한 라이브 정보 새로고침 실패:", err);
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
        console.error("❌ 라이브 정보 불러오기 실패:", err);
        setError("라이브 정보를 불러오는데 실패했습니다.");
        setLoading(false);
      });
  }, [roomId]);

  const leaveRoom = () => {
    disconnect();
    navigate("/live");
  };

     const handleSubmitBid = (amount: number) => {
     console.log("💰 입찰 전송: ", amount);
     console.log("🔍 현재 경매 정보:", currentAuction);
     console.log("🔍 현재 bids 상태:", bids);
     console.log("🔍 현재 latestBidPrice:", latestBidPrice);

     // liveData에서 직접 IN_PROGRESS 상태인 경매를 찾기
     const currentAuctionInProgress = liveData?.auctions.find(a => a.status === "IN_PROGRESS");
     
     if (!currentAuctionInProgress) {
       alert("진행 중인 경매를 찾을 수 없습니다.");
       return;
     }
                 sendMessage({
               type: "submitBid",
               roomId,
               auctionId: currentAuctionInProgress.id,
               bidPrice: amount,
             });
  };

  const currentAuction =
    liveData?.auctions.find((a) => a.status === "IN_PROGRESS") ||
    liveData?.auctions[0];

  const latestBidPrice =
    bids.length > 0
      ? Math.max(...bids.map((b) => b.bidPrice))
      : currentAuction?.startPrice ?? 0;

  console.log("🟡 최신 입찰가 latestBidPrice:", latestBidPrice);
  console.log("📋 bids 배열 길이:", bids.length);
  console.log("📋 bids 배열 내용:", bids);
  console.log("🏷️ currentAuction?.startPrice:", currentAuction?.startPrice);

  // AI Gesture hooks
  const { ready, infer } = useGestureModel("/models/best.onnx");
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // AI 제스처 입찰 관련 상태
  const [currentGesture, setCurrentGesture] = useState<string | null>(null);
  const [gestureStartTime, setGestureStartTime] = useState<number>(0);
  const [lastBidTime, setLastBidTime] = useState<number>(0);
  const currentGestureRef = useRef<string | null>(null);
  const gestureStartTimeRef = useRef<number>(0);
  const GESTURE_HOLD_TIME = 1500; // 1.5초 유지하면 입찰
  const BID_COOLDOWN = 3000; // 3초 쿨다운

  // MediaPipe + ONNX inference
  useEffect(() => {
    const video = localVideoRef.current;
    if (!video || !ready) return;

    const hands = new Hands({
      locateFile: (file) =>
        `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`,
    });
    hands.setOptions({
      maxNumHands: 1,
      modelComplexity: 1,
      minDetectionConfidence: 0.7,
      minTrackingConfidence: 0.5,
    });
    hands.onResults(async (results: Results) => {
      if (!canvasRef.current) return;
      const ctx = canvasRef.current.getContext("2d")!;
      canvasRef.current.width = video.videoWidth;
      canvasRef.current.height = video.videoHeight;
      ctx.clearRect(0, 0, video.videoWidth, video.videoHeight);

      if (!results.multiHandLandmarks?.length) return;
      const lm = results.multiHandLandmarks[0];
      const xs = lm.map((p) => p.x * video.videoWidth);
      const ys = lm.map((p) => p.y * video.videoHeight);
      const pad = 30;
      const x1 = Math.max(0, Math.min(...xs) - pad);
      const y1 = Math.max(0, Math.min(...ys) - pad);
      const x2 = Math.min(video.videoWidth, Math.max(...xs) + pad);
      const y2 = Math.min(video.videoHeight, Math.max(...ys) + pad);
      const w = x2 - x1;
      const h = y2 - y1;

      // Crop & resize
      const off = document.createElement("canvas");
      off.width = video.videoWidth;
      off.height = video.videoHeight;
      off.getContext("2d")!.drawImage(video, 0, 0);
      const crop = document.createElement("canvas");
      crop.width = w;
      crop.height = h;
      crop.getContext("2d")!.drawImage(off, x1, y1, w, h, 0, 0, w, h);
      const rz = document.createElement("canvas");
      rz.width = 160;
      rz.height = 160;
      rz.getContext("2d")!.drawImage(crop, 0, 0, w, h, 0, 0, 160, 160);
      const imageData = rz.getContext("2d")!.getImageData(0, 0, 160, 160);

      // ONNX inference
      const dets: GestureResult[] = await infer(imageData);

      // AI 제스처 입찰 처리
      if (dets.length) {
        const gesture = dets[0];
        const now = Date.now();

        // 제스처가 바뀌었거나 처음 감지된 경우
        if (gesture.className !== currentGestureRef.current) {
          currentGestureRef.current = gesture.className;
          setCurrentGesture(gesture.className);
          gestureStartTimeRef.current = now; // 시작 시간 기록
          setGestureStartTime(now);
          console.log(
            `🎯 새로운 제스처 감지: ${
              gesture.className
            } (${new Date().toLocaleTimeString()})`
          );
        }

        // 제스처 유지 시간 계산
        const holdTime = now - gestureStartTimeRef.current;

        // 1.5초 이상 유지하고, 쿨다운이 지난 경우
        if (
          holdTime >= GESTURE_HOLD_TIME &&
          now - lastBidTime >= BID_COOLDOWN
        ) {
          // 제스처 이름을 숫자로 변환 (one~ten 지원)
          let gestureNumber;
          if (gesture.className === "one") gestureNumber = 1;
          else if (gesture.className === "two") gestureNumber = 2;
          else if (gesture.className === "three") gestureNumber = 3;
          else if (gesture.className === "four") gestureNumber = 4;
          else if (gesture.className === "five") gestureNumber = 5;
          else if (gesture.className === "six") gestureNumber = 6;
          else if (gesture.className === "seven") gestureNumber = 7;
          else if (gesture.className === "eight") gestureNumber = 8;
          else if (gesture.className === "nine") gestureNumber = 9;
          else if (gesture.className === "ten") gestureNumber = 10;
          else if (gesture.className === "zero") gestureNumber = 0;
          else gestureNumber = parseInt(gesture.className);

          if (isNaN(gestureNumber)) {
            console.log(`⚠️ 지원하지 않는 제스처 감지: ${gesture.className}`);
            return;
          }
          const gestureAmount = gestureNumber * 1000; // 제스처 숫자 * 1000원
          // 현재 최신 입찰가를 latestBidPrice에서 가져오기 (실시간 업데이트됨)
          const bidAmount = latestBidPrice + gestureAmount;

          console.log(
            `💰 AI 제스처 입찰: ${gesture.className} (유지시간: ${holdTime}ms) → ${gestureAmount}원 추가 → ${latestBidPrice} + ${gestureAmount} = ${bidAmount}원`
          );

          // 수동 입찰과 동일한 방식으로 전송 (모달 확인 절차)
          console.log("🔍 AI 제스처 입찰 조건 체크:");
          console.log("  - currentAuction 존재:", !!currentAuction);
          console.log("  - currentAuction:", currentAuction);
          console.log("  - currentAuction?.status:", currentAuction?.status);
          console.log("  - liveData?.auctions:", liveData?.auctions);
          console.log("  - IN_PROGRESS 상태인 경매:", liveData?.auctions?.find(a => a.status === "IN_PROGRESS"));
          
                     // liveData에서 직접 IN_PROGRESS 상태인 경매를 찾기
           const currentAuctionInProgress = liveData?.auctions.find(a => a.status === "IN_PROGRESS");
           
           if (currentAuctionInProgress) {
            console.log("💰 AI 제스처 입찰 전송 시작:", bidAmount);
                         console.log("🔍 currentAuctionInProgress 확인:", currentAuctionInProgress);
             console.log("🔍 roomId 확인:", roomId);
             console.log("🔍 currentAuctionInProgress.id 확인:", currentAuctionInProgress.id);
            aiBidSuccessRef.current = true; // AI 제스처 입찰 플래그
            aiBidAmountRef.current = bidAmount; // AI 입찰 금액 저장
            setAiBidAmount(bidAmount); // 모달 표시용
            setShowAIBidModal(true); // 모달 표시
            setLastBidTime(now); // 쿨다운 갱신
            gestureStartTimeRef.current = 0; // 시작 시간 리셋
            setGestureStartTime(0);
            console.log(`✅ AI 제스처 입찰 모달 표시: ${bidAmount}원`);
          } else {
            console.log("❌ AI 제스처 입찰 조건 불만족:");
            console.log("  - currentAuction 존재:", !!currentAuction);
            console.log("  - currentAuction?.status:", currentAuction?.status);
            console.log("  - 조건 만족 여부:", currentAuction && currentAuction.status === "IN_PROGRESS");
            
                         // AI 제스처 입찰 조건 불만족 시 강제로 라이브 데이터 새로고침
             console.log("🔄 AI 제스처 입찰 조건 불만족으로 인한 라이브 데이터 강제 새로고침");
             
             // 재시도할 AI 입찰 정보를 저장
             setPendingAIBid({ amount: bidAmount, timestamp: now });
             
             refreshLiveDataSilently();
          }
        }
      } else {
        // 제스처가 사라진 경우
        if (currentGestureRef.current) {
          currentGestureRef.current = null;
          setCurrentGesture(null);
          gestureStartTimeRef.current = 0; // 시작 시간 리셋
          setGestureStartTime(0);
          console.log("🔄 제스처 사라짐");
        }
      }

      // Draw overlays
      ctx.strokeStyle = "#00F";
      ctx.lineWidth = 4;
      ctx.strokeRect(x1, y1, w, h);
      drawConnectors(ctx, lm, HAND_CONNECTIONS, {
        color: "#0F0",
        lineWidth: 2,
      });
      drawLandmarks(ctx, lm, { color: "#F00", lineWidth: 2 });
      if (dets.length) {
        const b = dets[0];
        ctx.fillStyle = "#F00";
        ctx.font = "24px Arial";
        ctx.fillText(
          `${b.className}: ${(b.confidence * 100).toFixed(1)}%`,
          x1,
          y1 - 10
        );

        // 제스처 유지 시간 표시
        if (currentGesture === b.className && gestureStartTime > 0) {
          const holdTime = Date.now() - gestureStartTime;
          const progress = Math.min(holdTime / GESTURE_HOLD_TIME, 1);
          ctx.fillStyle = progress >= 1 ? "#00FF00" : "#FFFF00";
          ctx.fillText(
            `Hold: ${(holdTime / 1000).toFixed(1)}s/${(
              GESTURE_HOLD_TIME / 1000
            ).toFixed(1)}s`,
            x1,
            y1 - 35
          );
        }
      }
    });

    const camera = new Camera(video, {
      onFrame: async () => {
        await hands.send({ image: video });
      },
      width: 1280,
      height: 720,
    });
    camera.start();

    return () => {
      hands.close();
      camera.stop();
    };
     }, [ready, infer, latestBidPrice, localVideoRef]);

   // liveData가 업데이트될 때 pendingAIBid 체크
   useEffect(() => {
     if (pendingAIBid && liveData) {
       console.log("🔄 liveData 업데이트 후 pendingAIBid 체크");
       const currentAuctionInProgress = liveData.auctions.find(a => a.status === "IN_PROGRESS");
       
       if (currentAuctionInProgress) {
         console.log("✅ liveData 업데이트 후 IN_PROGRESS 경매 발견:", currentAuctionInProgress);
         console.log("💰 pendingAIBid 처리:", pendingAIBid.amount);
         
         aiBidSuccessRef.current = true;
         aiBidAmountRef.current = pendingAIBid.amount;
         setAiBidAmount(pendingAIBid.amount);
         setShowAIBidModal(true);
         setLastBidTime(pendingAIBid.timestamp);
         gestureStartTimeRef.current = 0;
         setGestureStartTime(0);
         console.log(`✅ liveData 업데이트 후 AI 제스처 입찰 모달 표시: ${pendingAIBid.amount}원`);
         
         // pendingAIBid 리셋
         setPendingAIBid(null);
       }
     }
   }, [liveData, pendingAIBid]);

  const freshCheck = () => {
    if (!liveData) return;
    console.log("🍃 [PARTICIPANT] 신선도 체크 요청 전송");
    setFreshNess(null);
    sendMessage({
      type: "freshCheck",
      roomId: liveData.id,
    });
  };

  return (
		<div
			style={{
				display: "flex",
				padding: "24px",
				gap: "24px",
				backgroundColor: "#f8fafc",
				minHeight: "100vh",
			}}
		>
			<div style={{ flex: 2 }}>
				{liveData && (
					<div
						style={{
							color: "#1f2937",
							padding: "20px 0",
							fontWeight: "700",
							fontSize: "32px",
							textAlign: "left",
							marginBottom: "20px",
							borderBottom: "2px solid #e5e7eb",
						}}
					>
						{liveData.title}
					</div>
				)}
				<div style={{ position: "relative", marginBottom: "24px" }}>
					<video
						ref={remoteVideoRef}
						autoPlay
						playsInline
						style={{
							width: "100%",
							height: "450px",
							borderRadius: "16px",
							backgroundColor: "#000",
							objectFit: "cover",
							boxShadow: "0 10px 25px rgba(0, 0, 0, 0.1)",
						}}
						onLoadedMetadata={() => console.log("✅ 원격 비디오 메타데이터 로드됨")}
						onCanPlay={() => console.log("✅ 원격 비디오 재생 가능")}
						onPlay={() => console.log("🎬 원격 비디오 재생 시작됨")}
						onError={(e) => console.error("❌ 원격 비디오 에러:", e)}
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
						Live
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
						신선도 요청
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
						<p style={{ margin: 0 }}>{freshNess ? `신선도 : ${freshNess}` : ""}</p>
					</div>
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
							{(() => {
								const nextAuction = liveData.auctions.find((a) => a.status === "SCHEDULED");
								return (
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
										{nextAuction ? (
											<LiveAuctionCard
												productName={nextAuction.product.name}
												weight={Number(nextAuction.product.weight)}
												amount={nextAuction.amount}
												grade={nextAuction.product.grade}
												origin={nextAuction.product.origin}
												startPrice={nextAuction.startPrice}
											/>
										) : (
											<div
												style={{
													padding: "40px 20px",
													backgroundColor: "#ffffff",
													borderRadius: "12px",
													border: "1px solid #e5e7eb",
													textAlign: "center",
													boxShadow: "0 2px 8px rgba(0, 0, 0, 0.06)",
													minHeight: "120px",
													display: "flex",
													alignItems: "center",
													justifyContent: "center",
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
									</div>
								);
							})()}
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

				<div>
					<button
						onClick={() => setShowModal(true)}
						disabled={!currentAuction || currentAuction.status !== "IN_PROGRESS"}
						style={{
							backgroundColor: !currentAuction || currentAuction.status !== "IN_PROGRESS" ? "#9CA3AF" : "#22C55E",
							color: "white",
							width: "100%",
							padding: "16px",
							borderRadius: "12px",
							fontSize: "16px",
							fontWeight: "600",
							border: "none",
							cursor: !currentAuction || currentAuction.status !== "IN_PROGRESS" ? "not-allowed" : "pointer",
							transition: "all 0.2s ease",
							boxShadow:
								currentAuction && currentAuction.status === "IN_PROGRESS"
									? "0 4px 12px rgba(34, 197, 94, 0.3)"
									: "0 4px 12px rgba(0, 0, 0, 0.15)",
							opacity: !currentAuction || currentAuction.status !== "IN_PROGRESS" ? 0.6 : 1,
						}}
					>
						수동 입찰
					</button>
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
						라이브 나가기
					</button>
				</div>
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
				<div
					style={{
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
						내 화면
					</h3>
					<div style={{ position: "relative" }}>
						<video
							ref={localVideoRef}
							autoPlay
							playsInline
							muted
							style={{
								width: "100%",
								height: "200px",
								border: "2px solid #3b82f6",
								borderRadius: "12px",
								backgroundColor: "#000",
								boxShadow: "0 4px 12px rgba(59, 130, 246, 0.2)",
								objectFit: "cover",
								objectPosition: "center",
							}}
							onLoadedMetadata={() => console.log("✅ 로컬 비디오 메타데이터 로드됨")}
							onCanPlay={() => console.log("✅ 로컬 비디오 재생 가능")}
							onError={(e) => console.error("❌ 로컬 비디오 에러:", e)}
						/>
						<canvas
							ref={canvasRef}
							style={{
								position: "absolute",
								top: 0,
								left: 0,
								width: "100%",
								height: "200px",
								pointerEvents: "none",
								borderRadius: "12px",
							}}
						/>
					</div>
				</div>

				{/* 수신호 제스처 가이드 버튼 */}
				<div
					style={{
						backgroundColor: "#f0fdf4",
						padding: "16px",
						borderRadius: "12px",
						border: "2px solid #22c55e",
						textAlign: "center",
					}}
				>
					<button
						onClick={() => setShowGestureGuideModal(true)}
						style={{
							backgroundColor: "#22c55e",
							color: "white",
							border: "none",
							padding: "12px 20px",
							borderRadius: "8px",
							fontSize: "14px",
							fontWeight: "600",
							cursor: "pointer",
							transition: "all 0.2s ease",
							display: "flex",
							alignItems: "center",
							justifyContent: "center",
							gap: "8px",
							width: "100%",
						}}
						onMouseEnter={(e) => {
							e.currentTarget.style.backgroundColor = "#16a34a";
							e.currentTarget.style.transform = "translateY(-1px)";
							e.currentTarget.style.boxShadow = "0 4px 12px rgba(34, 197, 94, 0.3)";
						}}
						onMouseLeave={(e) => {
							e.currentTarget.style.backgroundColor = "#22c55e";
							e.currentTarget.style.transform = "translateY(0)";
							e.currentTarget.style.boxShadow = "none";
						}}
					>
						<span style={{ fontSize: "16px" }}>🤚</span>
						수신호 제스처 가이드 보기
					</button>
					<p
						style={{
							fontSize: "12px",
							color: "#15803d",
							margin: "8px 0 0 0",
							fontWeight: "500",
						}}
					>
						손가락 제스처로 편리하게 입찰하세요!
					</p>
				</div>

				<BidList bids={bids} />

				{liveData && (
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
						<AuctionList auctions={liveData.auctions} />
					</div>
				)}
			</div>

			{showModal && currentAuction && (
				<BidModal
					key={`modal-${latestBidPrice}`} // 중요!
					currentPrice={latestBidPrice}
					productName={currentAuction.product.name}
					weight={Number(currentAuction.product.weight)}
					product_amount={currentAuction.amount}
					onClose={() => setShowModal(false)}
					onSubmit={handleSubmitBid}
				/>
			)}

			{showAIBidModal && aiBidAmount > 0 && currentAuction && (
				<BidModal
					key={`ai-bid-modal-${aiBidAmount}`}
					currentPrice={latestBidPrice}
					productName={currentAuction.product.name}
					weight={Number(currentAuction.product.weight) * (currentAuction.amount || 1)}
					product_amount={currentAuction.amount}
					onClose={() => {
						setShowAIBidModal(false);
						setAiBidAmount(0);
						aiBidSuccessRef.current = false; // 모달 닫을 때 리셋
						aiBidAmountRef.current = 0; // 모달 닫을 때 리셋
					}}
					onSubmit={(amount) => {
						handleSubmitBid(amount);
						setShowAIBidModal(false);
						setAiBidAmount(0);
						aiBidSuccessRef.current = false; // 모달 닫을 때 리셋
						aiBidAmountRef.current = 0; // 모달 닫을 때 리셋
					}}
					isAIBid={true}
					aiBidAmount={aiBidAmount}
				/>
			)}

			{/* 안내 모달 */}
			<ParticipantGuideModal isOpen={showGuideModal} onClose={() => setShowGuideModal(false)} />

			{/* 제스처 가이드 모달 */}
			<GestureGuideModal isOpen={showGestureGuideModal} onClose={() => setShowGestureGuideModal(false)} />
		</div>
	);
};

export default LiveParticipantView;
