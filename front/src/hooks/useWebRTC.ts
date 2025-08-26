import { useEffect, useRef } from "react";
import { useUserStore } from "../stores/useUserStore";
import axiosInstance from "../api/axiosInstance";

const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
const SERVER_URL = import.meta.env.VITE_SERVER_URL;
const SIGNALING_SERVER_URL = `${protocol}//${SERVER_URL}/api/call`;

const EC2_PUBLIC_IP = import.meta.env.VITE_EC2_PUBLIC_IP;
const TURNUSER = import.meta.env.VITE_TURNUSER;
const TURNPASS = import.meta.env.VITE_TURNPASS;
const ICE_SERVERS: RTCIceServer[] = [
	{ urls: "stun:stun.l.google.com:19302" },
	{ urls: "stun:stun1.l.google.com:19302" },
	{ urls: "stun:stun2.l.google.com:19302" },
	{ urls: "stun:stun3.l.google.com:19302" },
	{ urls: "stun:stun4.l.google.com:19302" },
	{
		urls: [`turn:${EC2_PUBLIC_IP}:3478?transport=udp`, `turn:${EC2_PUBLIC_IP}:3478?transport=tcp`],
		username: TURNUSER,
		credential: TURNPASS,
	},
];

interface OfferMessage {
	type: "offer";
	sdpOffer: string;
	roomId: number;
}

interface AnswerMessage {
	type: "answer";
	sdpAnswer: string;
}

interface IceCandidateMessage {
	type: "iceCandidate";
	candidate: RTCIceCandidateInit;
}

interface NewParticipantArrivedMessage {
	type: "newParticipantArrived";
	userId: string;
}

export interface BidStatusMessage {
	type: "bidStatusUpdate";
	bidList: {
		userNickName: string;
		bidPrice: number;
		bidTime?: string;
	}[];
	currentHighestPrice: number;
	auctionId: number;
	status?: string; // status 필드 추가
}

interface AuctionResultMessage {
	type: "startAuctionResult" | "stopAuctionResult" | "submitBidResult";
	success: boolean;
	message: string;
}

interface ErrorMessage {
	type: "error";
	message: string;
}

interface FreshNessRequestMessage {
	type: "freshNessRequest";
	success: boolean;
	message: string;
}

interface FreshNessResultMessage {
	type: "freshNessResult";
	message: string;
}

interface WinningBidResultMessage {
	type: "winningBidResult";
	success: boolean;
	message: string;
	bidPrice?: number;
	userId?: number;
}

interface LeaveParticipantMessage {
	type: "leaveParticipant";
}

type WebSocketMessage =
	| OfferMessage
	| AnswerMessage
	| IceCandidateMessage
	| NewParticipantArrivedMessage
	| BidStatusMessage
	| AuctionResultMessage
	| ErrorMessage
	| FreshNessRequestMessage
	| FreshNessResultMessage
	| WinningBidResultMessage
	| LeaveParticipantMessage
	| { type: string; [key: string]: unknown };

export function useWebRTC(
	roomId: number,
	role: "host" | "participant",
	onMessage?: (
		msg:
			| BidStatusMessage
			| AuctionResultMessage
			| FreshNessRequestMessage
			| FreshNessResultMessage
			| WinningBidResultMessage
			| LeaveParticipantMessage
	) => void
) {
	const answeredRef = useRef(false);
	const wsRef = useRef<WebSocket | null>(null);
	const pcRef = useRef<RTCPeerConnection | null>(null);
	const localVideoRef = useRef<HTMLVideoElement | null>(null);
	const remoteVideoRef = useRef<HTMLVideoElement | null>(null);
	const localStreamRef = useRef<MediaStream | null>(null);
	const pendingCandidatesRef = useRef<RTCIceCandidateInit[]>([]);

	const sendMessage = (msg: Record<string, unknown>) => {
		const ws = wsRef.current;
		if (!ws) return;
		const send = () => ws.send(JSON.stringify(msg));
		if (ws.readyState === WebSocket.CONNECTING) {
			ws.addEventListener("open", send, { once: true });
		} else if (ws.readyState === WebSocket.OPEN) {
			send();
		}
	};

	const processPendingCandidates = async () => {
		if (!pcRef.current) return;
		while (pendingCandidatesRef.current.length > 0) {
			const candidate = pendingCandidatesRef.current.shift();
			if (candidate) {
				try {
					await pcRef.current.addIceCandidate(new RTCIceCandidate(candidate));
					console.log("✅ Pending ICE candidate added successfully");
				} catch (err) {
					console.error("❌ Failed to add pending ICE candidate:", err);
				}
			}
		}
	};

	const disconnect = () => {
		console.log("🧹 WebRTC 정리");
		if (wsRef.current?.readyState === WebSocket.OPEN) {
			sendMessage({ type: "stop", roomId });
			wsRef.current.close();
		}
		pcRef.current?.close();
		pcRef.current = null;
		localStreamRef.current?.getTracks().forEach((t) => t.stop());
		localStreamRef.current = null;
		pendingCandidatesRef.current = [];
	};
	useEffect(() => {
		const connectWebSocket = () => {
			// JWT 토큰 검증
			const token = useUserStore.getState().accessToken;
			if (!token) {
				console.error("❌ JWT 토큰이 없습니다. WebSocket 연결을 중단합니다.");
				return;
			}

			const wsUrl = `${SIGNALING_SERVER_URL}?token=${token}`;
			console.log("🔌 WebSocket 연결 시도:", wsUrl.replace(/token=[\w.-]+/, "token=***"));
			const ws = new WebSocket(wsUrl);
			wsRef.current = ws;

			ws.onopen = async () => {
				console.log("✅ WebSocket 연결 성공");
				try {
					// 간단한 카메라 요청
					const localStream = await navigator.mediaDevices.getUserMedia({
						video: true,
						audio: true, //호스트는 오디오가 들려야함
					});
					localStreamRef.current = localStream;

					if (localVideoRef.current) {
						localVideoRef.current.srcObject = localStream;
						console.log("✅ 로컬 비디오 스트림 설정 완료");
					}

					const pc = new RTCPeerConnection({ iceServers: ICE_SERVERS });
					pcRef.current = pc;

					localStream.getTracks().forEach((track) => {
						pc.addTrack(track, localStream);
						console.log("✅ 트랙 추가:", track.kind);
					});

					if (role !== "host") {
						localStream.getAudioTracks().forEach((track) => {
							track.enabled = false; // 마이크 꺼짐
						});
					}

					pc.onicecandidate = (event) => {
						if (event.candidate) {
							console.log("🧊 ICE candidate 생성:", event.candidate);
							sendMessage({
								type: "onIceCandidate",
								roomId,
								candidate: event.candidate,
							});
						}
					};

					// 원격 스트림 수신
					pc.ontrack = (event) => {
						console.log("🎥 Remote stream received from server");
						const stream = event.streams[0];

						if (remoteVideoRef.current) {
							remoteVideoRef.current.srcObject = stream;
						}
					};

					pc.oniceconnectionstatechange = () => {
						console.log("🧊 ICE 연결 상태:", pc.iceConnectionState);
					};

					pc.onconnectionstatechange = () => {
						console.log("🔗 연결 상태:", pc.connectionState);
					};

					// Host와 Participant 모두 Offer 생성
					const offer = await pc.createOffer();
					await pc.setLocalDescription(offer);

					if (role === "host") {
						console.log("🎤 [HOST] Host Offer 전송");
						sendMessage({ type: "host", roomId, sdpOffer: offer.sdp });
					} else {
						console.log("👤 [PARTICIPANT] Participant Offer 전송");
						sendMessage({ type: "participant", roomId, sdpOffer: offer.sdp });
					}
				} catch (error) {
					console.error("❌ Failed to initialize WebRTC:", error);
				}
			};

			ws.onmessage = async (msg) => {
				let data: WebSocketMessage;
				try {
					data = JSON.parse(msg.data) as WebSocketMessage;
					console.log(`📨 [${role.toUpperCase()}] WebSocket 메시지 수신:`, data.type);
				} catch (error) {
					console.error("❌ WebSocket JSON parse error:", error);
					return;
				}

				switch (data.type) {
					case "startResponse":
						console.log(`✅ [${role.toUpperCase()}] Start Response 수신`);
						// success 필드 확인
						if ("success" in data && data.success === false) {
							console.error(`❌ [${role.toUpperCase()}] Start Response 실패:`, data.message || "알 수 없는 오류");
							alert(`연결 실패: ${data.message || "알 수 없는 오류"}`);
							return;
						}
						if (pcRef.current && !answeredRef.current && "sdpAnswer" in data) {
							await pcRef.current.setRemoteDescription(
								new RTCSessionDescription({
									type: "answer",
									sdp: data.sdpAnswer as string,
								})
							);
							await processPendingCandidates();
							answeredRef.current = true;
							console.log(`✅ [${role.toUpperCase()}] Remote Description 설정 완료`);
						}
						break;

					case "offer":
						console.log(`📤 [${role.toUpperCase()}] Offer 수신`);
						if (pcRef.current && "sdpOffer" in data) {
							console.log(`📤 [${role.toUpperCase()}] Offer SDP 처리 중...`);
							try {
								await pcRef.current.setRemoteDescription(
									new RTCSessionDescription({
										type: "offer",
										sdp: data.sdpOffer as string,
									})
								);
								await processPendingCandidates();
								const answer = await pcRef.current.createAnswer();
								await pcRef.current.setLocalDescription(answer);
								console.log(`📤 [${role.toUpperCase()}] Answer 전송`);
								sendMessage({
									type: "answer",
									roomId,
									sdpAnswer: answer.sdp,
								});
							} catch (error) {
								console.error(`❌ [${role.toUpperCase()}] Offer 처리 실패:`, error);
							}
						} else {
							console.warn(`⚠️ [${role.toUpperCase()}] Offer 처리 불가: pcRef 또는 sdpOffer 없음`);
						}
						break;
					case "iceCandidate":
						console.log(`🧊 [${role.toUpperCase()}] ICE Candidate 수신`);
						if (pcRef.current && "candidate" in data) {
							const candidateData = data as IceCandidateMessage;
							if (pcRef.current.remoteDescription) {
								try {
									await pcRef.current.addIceCandidate(new RTCIceCandidate(candidateData.candidate));
									console.log("✅ ICE candidate 추가 성공");
								} catch (err) {
									console.error("❌ ICE candidate 추가 실패:", err);
								}
							} else {
								pendingCandidatesRef.current.push(candidateData.candidate);
								console.log("📦 ICE candidate 대기열에 추가");
							}
						}
						break;
					case "answer":
						console.log(`📤 [${role.toUpperCase()}] Answer 수신`);
						if (pcRef.current && !answeredRef.current && "sdpAnswer" in data) {
							console.log(`📤 [${role.toUpperCase()}] Answer SDP 처리 중...`);
							await pcRef.current.setRemoteDescription(
								new RTCSessionDescription({
									type: "answer",
									sdp: data.sdpAnswer as string,
								})
							);
							await processPendingCandidates();
							answeredRef.current = true;
							console.log(`✅ [${role.toUpperCase()}] Remote Description 설정 완료`);
						}
						break;

					case "bidStatusUpdate":
						console.log(`💰 [${role.toUpperCase()}] Bid Status Update 수신`);
						if ("bidListJson" in data && typeof data.bidListJson === "string") {
							try {
								const parsedList = JSON.parse(data.bidListJson);
								console.log(`💰 [${role.toUpperCase()}] Bid List 파싱 성공:`, parsedList);
								onMessage?.({
									type: "bidStatusUpdate",
									bidList: parsedList,
									currentHighestPrice: Number(data.currentHighestPrice),
									auctionId: Number(data.auctionId),
									status: typeof data.status === "string" ? data.status : undefined, // status 필드 추가
								});
							} catch (err) {
								console.error(`❌ [${role.toUpperCase()}] Failed to parse bidListJson:`, err);
							}
						} else {
							console.warn(`⚠️ [${role.toUpperCase()}] bidStatusUpdate message is malformed:`, data);
						}
						break;

					case "startAuctionResult":
					case "stopAuctionResult":
					case "submitBidResult":
						console.log(`🎯 [${role.toUpperCase()}] Auction Result 수신:`, data);
						const resultData = data as AuctionResultMessage;

						// 실패한 경우 에러 메시지 표시
						if (resultData.success === false) {
							console.warn(`⚠️ [${role.toUpperCase()}] ${resultData.type} 실패:`, resultData.message);
							if (resultData.type === "submitBidResult") {
								alert(`입찰 실패: ${resultData.message}`);
							} else {
								alert(`경매 작업 실패: ${resultData.message}`);
							}
						}

						onMessage?.(resultData);
						break;

					case "freshNessRequest":
						console.log(`🍃 [${role.toUpperCase()}] 신선도 요청 메시지 수신:`, data);
						onMessage?.(data as FreshNessRequestMessage);
						break;

					case "freshNessResult":
						console.log(`🍃 [${role.toUpperCase()}] 신선도 결과 메시지 수신:`, data);
						onMessage?.(data as FreshNessResultMessage);
						break;

					case "winningBidResult":
						console.log(`🎉 [${role.toUpperCase()}] 낙찰 결과 메시지 수신:`, data);
						onMessage?.(data as WinningBidResultMessage);
						break;

					case "leaveParticipant":
						console.log(`🚪 [${role.toUpperCase()}] 라이브 종료 메시지 수신:`, data);
						onMessage?.(data as LeaveParticipantMessage);
						break;

					case "error":
						const errorMessage = (data as ErrorMessage).message || "Unknown error";
						console.error(`❌ [${role.toUpperCase()}] WebRTC error:`, errorMessage);

						// 권한 관련 에러 처리
						if (
							errorMessage.includes("권한") ||
							errorMessage.includes("호스트") ||
							errorMessage.includes("판매자") ||
							errorMessage.includes("No host available")
						) {
							alert("호스트가 라이브를 시작하기 전입니다!");
							window.location.href = "/";
						} else if (errorMessage.includes("라이브")) {
							alert(`라이브 상태 오류: ${errorMessage}`);
						} else {
							alert(`오류: ${errorMessage}`);
						}
						break;

					default:
						console.warn(`⚠️ [${role.toUpperCase()}] Unhandled WebSocket message:`, data);
				}
			};

			ws.onerror = (err) => {
				console.error("❌ WebSocket ERROR", err);
				setTimeout(connectWebSocket, 3000);
			};

			ws.onclose = async (e) => {
				console.log("🔌 WebSocket CLOSED", e.code, e.reason || "No reason");

				// 인증 관련 에러 (1006: 비정상 종료, 즉시 닫힘)
				if (e.code === 1006) {
					console.warn("⚠️ 인증 실패 감지, 토큰 갱신 시도");
					try {
						// 새 토큰 요청
						const response = await axiosInstance.get("/util/check-token");
						if (response.status === 200) {
							console.log("✅ 토큰 갱신 성공, 재연결 시도");
							setTimeout(connectWebSocket, 1000);
						}
					} catch (error) {
						console.error("❌ 토큰 갱신 실패:", error);
						// 로그인 페이지 리디렉션 등 처리 가능
					}
					return;
				}

				// 기타 연결 오류는 재연결 시도
				if (e.code !== 1000) {
					setTimeout(connectWebSocket, 3000);
				}
			};
		};

		connectWebSocket();

		return () => {
			disconnect();
		};
	}, [roomId, role]);

	return { localVideoRef, remoteVideoRef, sendMessage, disconnect };
}
