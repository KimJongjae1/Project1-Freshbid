import React, { useState, useEffect } from "react";

interface BidItem {
  userNickName: string;
  bidPrice: number;
  bidTime?: string; // ISO 8601 형식의 시간 문자열
}

interface BidListProps {
  bids: BidItem[];
}

// 상대적 시간 계산 함수
const getRelativeTime = (bidTime: string): string => {
  const now = new Date();
  const bidDate = new Date(bidTime);
  const diffInSeconds = Math.floor((now.getTime() - bidDate.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return `${diffInSeconds}초 전`;
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes}분 전`;
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours}시간 전`;
  } else {
    const days = Math.floor(diffInSeconds / 86400);
    return `${days}일 전`;
  }
};

const BidList: React.FC<BidListProps> = ({ bids }) => {
  const [elapsedTime, setElapsedTime] = useState<string>("");

  // 마지막 입찰 시간 계산 및 실시간 업데이트
  useEffect(() => {
    if (bids.length === 0) {
      setElapsedTime("");
      return;
    }

    // 가장 최근 입찰 찾기 (bidTime이 있는 경우)
    const lastBid = bids.find(bid => bid.bidTime);
    if (!lastBid?.bidTime) {
      setElapsedTime("");
      return;
    }

    // 초기 시간 설정
    setElapsedTime(getRelativeTime(lastBid.bidTime));

    // 1초마다 시간 업데이트
    const interval = setInterval(() => {
      if (lastBid.bidTime) {
        setElapsedTime(getRelativeTime(lastBid.bidTime));
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [bids]);

  return (
    <div style={{
      backgroundColor: "#f9fafb",
      padding: "20px",
      borderRadius: "16px",
      border: "1px solid #e5e7eb",
      minHeight: "300px",
      maxHeight: "300px"
    }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
        <h3 style={{ fontSize: "18px", fontWeight: "700", color: "#1f2937", margin: 0 }}>입찰현황</h3>
        {elapsedTime && (
          <span style={{
            fontSize: "12px",
            color: "#6b7280",
            backgroundColor: "#f3f4f6",
            padding: "4px 8px",
            borderRadius: "6px"
          }}>
            마지막 입찰 {elapsedTime}
          </span>
        )}
      </div>

             <div style={{ 
         maxHeight: "220px", 
         overflowY: "auto"
       }}>
         {bids.length > 0 ? (
           <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
            {bids.map((bid, index) => (
                             <li
                 key={index}
                 style={{
                   padding: "12px",
                   marginBottom: "8px",
                   border: "1px solid #e5e7eb",
                   borderRadius: "8px",
                   backgroundColor: "#ffffff"
                 }}
               >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontWeight: "600", color: "#374151" }}>
                    {bid.userNickName}
                  </span>
                  <span style={{ fontWeight: "700", color: "#059669", fontSize: "16px" }}>
                    {bid.bidPrice.toLocaleString()}원
                  </span>
                </div>
                {bid.bidTime && (
                  <div style={{ fontSize: "12px", color: "#6b7280", marginTop: "4px" }}>
                    {getRelativeTime(bid.bidTime)}
                  </div>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <div style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "120px",
            color: "#6b7280",
            fontSize: "16px"
          }}>
            입찰 기록이 없습니다
          </div>
        )}
      </div>
    </div>
  );
};

export default BidList;
