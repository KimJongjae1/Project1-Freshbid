import { useEffect, useRef, useState, useCallback } from "react";

export interface GestureResult {
	className: string;
	confidence: number;
	bbox: number[];
}

export interface HandBoundingBox {
	x1: number;
	y1: number;
	x2: number;
	y2: number;
}

export function useGestureModel(modelPath: string) {
	const [ready, setReady] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(false);
	const sessionRef = useRef<any>(null);
	const ortRef = useRef<any>(null);
	const initializingRef = useRef(false);
	const readyRef = useRef(false);

	// 🔥 무한 호출 방지를 위한 플래그
	const isInferringRef = useRef(false);

	useEffect(() => {
		readyRef.current = ready;
	}, [ready]);

	useEffect(() => {
		let isMounted = true;

		const initModel = async () => {
			if (initializingRef.current) {
				return;
			}

			initializingRef.current = true;
			setLoading(true);
			setError(null);
			setReady(false);
			readyRef.current = false;

			try {
				let ort = (window as any).ort;

				if (!ort) {
					const script = document.createElement("script");
					script.src = "https://cdn.jsdelivr.net/npm/onnxruntime-web@1.16.3/dist/ort.min.js";
					document.head.appendChild(script);

					await new Promise((resolve, reject) => {
						const timeout = setTimeout(() => {
							reject(new Error("ONNX Runtime 로딩 타임아웃"));
						}, 10000);

						const checkOrt = () => {
							if ((window as any).ort) {
								clearTimeout(timeout);
								ort = (window as any).ort;
								resolve(ort);
							} else {
								setTimeout(checkOrt, 100);
							}
						};

						script.onload = checkOrt;
						script.onerror = () => {
							clearTimeout(timeout);
							reject(new Error("ONNX Runtime 스크립트 로드 실패"));
						};
					});
				}

				ortRef.current = ort;

				if (!ort?.InferenceSession?.create) {
					throw new Error("InferenceSession.create 메서드를 찾을 수 없습니다");
				}

				try {
					ort.env.wasm.simd = true;
					ort.env.wasm.numThreads = 1;
					ort.env.wasm.wasmPaths = "https://cdn.jsdelivr.net/npm/onnxruntime-web@1.16.3/dist/";
				} catch (envError) {
					console.warn("⚠️ 환경 설정 실패:", envError);
				}

				const sessionOptions = {
					executionProviders: ["wasm"],
					graphOptimizationLevel: "all",
				};

				const session = await ort.InferenceSession.create(modelPath, sessionOptions);

				if (isMounted) {
					sessionRef.current = session;

					setReady(true);
					readyRef.current = true;
					setError(null);
				}
			} catch (error) {
				const errorMessage = error instanceof Error ? error.message : String(error);
				if (isMounted) {
					setError(errorMessage);
					setReady(false);
					readyRef.current = false;
				}
			} finally {
				if (isMounted) {
					setLoading(false);
				}
				initializingRef.current = false;
			}
		};

		initModel();

		return () => {
			isMounted = false;
			if (sessionRef.current) {
				try {
					sessionRef.current.release?.();
				} catch (e) {
					console.warn("세션 해제 중 오류:", e);
				}
				sessionRef.current = null;
			}
			initializingRef.current = false;
		};
	}, [modelPath]);

	const infer = useCallback(
		async (imageData: ImageData, _handBbox?: HandBoundingBox, _landmarks?: any[]): Promise<GestureResult[]> => {
			// 🔥 이미 추론 중이면 스킵
			if (isInferringRef.current) {
				return [];
			}

			// 🔥 기본 조건 체크
			if (loading || !readyRef.current || !sessionRef.current || !ortRef.current) {
				return [];
			}

			// 🔥 추론 시작 플래그 설정
			isInferringRef.current = true;

			try {
				const { width, height, data } = imageData;
				const total = width * height;
				const inputTensor = new Float32Array(3 * total);
				for (let i = 0; i < total; i++) {
					inputTensor[i] = data[i * 4] / 255; // R
					inputTensor[total + i] = data[i * 4 + 1] / 255; // G
					inputTensor[2 * total + i] = data[i * 4 + 2] / 255; // B
				}
				const tensor = new ortRef.current.Tensor("float32", inputTensor, [1, 3, height, width]);

				const inputName = sessionRef.current.inputNames[0];
				const feeds = { [inputName]: tensor };
				const results = await sessionRef.current.run(feeds);
				const outputName = sessionRef.current.outputNames[0];
				const output = results[outputName];

				const detections = postprocess(output.data as Float32Array, output.dims as number[]);
				return detections;
			} catch (error) {
				console.error("❌ 추론 중 오류:", error);
				return [];
			} finally {
				// 🔥 추론 완료 플래그 해제
				isInferringRef.current = false;
			}
		},
		[loading]
	);

	return {
		ready,
		error,
		loading,
		infer,
		hasSession: !!sessionRef.current,
		hasOrt: !!ortRef.current,
	};
}

function postprocess(data: Float32Array, dims: number[]): GestureResult[] {
	const [, numFeatures, numBoxes] = dims;
	const confThreshold = 0.1;
	const numClasses = numFeatures - 4;
	const detections: { classId: number; confidence: number; bbox: number[] }[] = [];

	// 1) 모든 박스 수집
	for (let i = 0; i < numBoxes; i++) {
		const centerX = data[0 * numBoxes + i];
		const centerY = data[1 * numBoxes + i];
		const width = data[2 * numBoxes + i];
		const height = data[3 * numBoxes + i];

		// 클래스별 확률
		const classProbs = Array.from({ length: numClasses }, (_, c) => data[(4 + c) * numBoxes + i]);
		const maxProb = Math.max(...classProbs);
		const classId = classProbs.indexOf(maxProb);

		if (maxProb > confThreshold) {
			const x1 = Math.max(0, centerX - width / 2);
			const y1 = Math.max(0, centerY - height / 2);
			const x2 = Math.min(1, centerX + width / 2);
			const y2 = Math.min(1, centerY + height / 2);
			detections.push({ classId, confidence: maxProb, bbox: [x1, y1, x2, y2] });
		}
	}

	if (detections.length === 0) return [];

	// 2) 클래스별 카운트
	const countMap = detections.reduce<Record<number, number>>((acc, det) => {
		acc[det.classId] = (acc[det.classId] || 0) + 1;
		return acc;
	}, {});

	// 3) 가장 많이 검출된 클래스 선택
	const bestClass = Object.entries(countMap).reduce((a, b) => (b[1] > a[1] ? b : a), ["0", 0])[0];
	const bestClassId = Number(bestClass);

	// 4) 해당 클래스 중 최고 confidence 박스 하나만 반환
	const bestDet = detections
		.filter((det) => det.classId === bestClassId)
		.reduce((prev, cur) => (cur.confidence > prev.confidence ? cur : prev));

	console.log(
		`✅ 최종 선택된 제스처: ${getClassName(bestClassId)} (${(bestDet.confidence * 100).toFixed(1)}%) - count ${
			countMap[bestClassId]
		}`
	);

	return [
		{
			className: getClassName(bestClassId),
			confidence: bestDet.confidence,
			bbox: bestDet.bbox,
		},
	];
}

function getClassName(classId: number): string {
	const classNames = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"];
	return classNames[classId] || `class_${classId}`;
}
