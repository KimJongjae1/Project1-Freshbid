import React, { useState, useMemo, useEffect } from "react";

interface BidModalProps {
  currentPrice: number;
  productName: string;
  weight: number;
  product_amount: number;
  onClose: () => void;
  onSubmit: (amount: number) => void;
  // AI 입찰용 props 추가
  isAIBid?: boolean;
  aiBidAmount?: number;
}

const BidModal: React.FC<BidModalProps> = ({
  currentPrice,
  productName,
  weight,
  product_amount,
  onClose,
  onSubmit,
  isAIBid = false,
  aiBidAmount = 0,
}) => {
  const [amount, setAmount] = useState("");
  const [confirming, setConfirming] = useState(false);
  const [basePrice, setBasePrice] = useState(currentPrice); // 상태로 따로 관리

  // currentPrice가 변경되면 basePrice도 반영
  useEffect(() => {
    setBasePrice(currentPrice);
  }, [currentPrice]);

  // AI 입찰 모드일 때는 바로 확인 단계로
  useEffect(() => {
    if (isAIBid && aiBidAmount > 0) {
      setConfirming(true);
    }
  }, [isAIBid, aiBidAmount]);

  const bid = parseInt(amount);
  const validBid = !isNaN(bid) && bid > 0;
  const bidAmount = bid * 1000;

  const totalBid = useMemo(() => {
    // AI 입찰 모드일 때는 aiBidAmount 사용
    if (isAIBid && aiBidAmount > 0) {
      return aiBidAmount;
    }
    return basePrice + bidAmount;
  }, [basePrice, bidAmount, isAIBid, aiBidAmount]);

  const handleInitialSubmit = () => {
    if (!validBid) {
      alert("천원 단위의 유효한 숫자를 입력해주세요.");
      return;
    }
    setConfirming(true);
  };

  const handleConfirm = () => {
    onSubmit(totalBid);
    onClose();
  };

  const handleCancelConfirm = () => {
    setConfirming(false);
    // AI 입찰 모드일 때는 모달을 닫음
    if (isAIBid) {
      onClose();
    }
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        backgroundColor: "rgba(0,0,0,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
      }}
    >
      <div
        style={{
          background: "white",
          padding: "24px",
          borderRadius: "12px",
          width: "360px",
          border: "1px solid black",
          position: "relative",
          textAlign: "center",
        }}
      >
        <button
          onClick={onClose}
          style={{
            position: "absolute",
            top: "12px",
            right: "12px",
            fontSize: "18px",
            fontWeight: "bold",
            background: "none",
            border: "none",
            cursor: "pointer",
          }}
        >
          X
        </button>

        <h2 style={{ fontSize: "20px", fontWeight: "bold", marginBottom: "4px" }}>
          {isAIBid ? "AI 수신호 입찰" : "수동입찰"}
        </h2>
        <p style={{ color: "#888", marginBottom: "12px" }}>
          {productName} {weight}KG {product_amount}박스
        </p>

        {confirming ? (
          <>
            <p style={{ fontSize: "16px", margin: "16px 0" }}>
              {productName} {weight}KG {product_amount}박스를 {totalBid.toLocaleString()}원에 입찰하시겠습니까?
            </p>
            {isAIBid && (
              <p style={{ fontSize: "14px", color: "#666", marginBottom: "16px" }}>
                🎯 수신호로 감지된 입찰가입니다
              </p>
            )}
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                gap: "12px",
                marginBottom: "24px",
              }}
            >
              <button
                onClick={handleConfirm}
                style={{
                  backgroundColor: "#7DD87D",
                  color: "white",
                  padding: "8px 20px",
                  borderRadius: "8px",
                  fontWeight: "bold",
                  border: "none",
                  cursor: "pointer",
                }}
              >
                입찰
              </button>
              <button
                onClick={handleCancelConfirm}
                style={{
                  backgroundColor: "#F87171",
                  color: "white",
                  padding: "8px 20px",
                  borderRadius: "8px",
                  fontWeight: "bold",
                  border: "none",
                  cursor: "pointer",
                }}
              >
                취소
              </button>
            </div>
          </>
        ) : (
          <>
            <p style={{ fontSize: "16px", fontWeight: "bold", marginBottom: "4px" }}>
              현재 가격: {basePrice.toLocaleString()}원
            </p>
            <p style={{ fontSize: "12px", color: "#999", marginBottom: "16px" }}>
              천원 단위로 입력해주세요!
            </p>

            <div
              style={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >
              <span style={{ fontSize: "20px", marginRight: "8px" }}>+</span>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="입찰가"
                style={{
                  width: "100px",
                  padding: "6px",
                  textAlign: "center",
                  fontSize: "16px",
                  border: "1px solid #ccc",
                  borderRadius: "6px",
                }}
              />
              <span style={{ fontSize: "16px", marginLeft: "8px" }}>천 원</span>
            </div>

            <button
              onClick={handleInitialSubmit}
              style={{
                backgroundColor: "#7DD87D",
                color: "white",
                padding: "10px 24px",
                borderRadius: "8px",
                fontSize: "16px",
                fontWeight: "bold",
                border: "none",
                cursor: "pointer",
              }}
            >
              입찰 하기
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default BidModal;
