import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";

// 주문 정보 타입 정의
interface OrderInfo {
  id: number;
  productName: string;
  price: number;
  createdAt: string;
  sellerName: string;
  sellerId: number;
  reprImgSrc: string;
  status: string;
  auctionTitle?: string;
}

const PurchasedList = () => {
  const [orders, setOrders] = useState<OrderInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // 주문 목록 조회
  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.get("/orders");
      const orderData = response.data.data || [];

      console.log("=== OrderResponseDto 원본 데이터 ===");
      console.log("주문 데이터:", orderData);

      // 각 주문의 sellerId 확인
      orderData.forEach((order: any, index: number) => {
        console.log(`주문 ${index + 1}:`, {
          id: order.id,
          sellerId: order.sellerId,
          status: order.status,
          auctionId: order.auctionId,
        });
      });

      // OrderResponseDto는 단순한 구조이므로, 상세 정보를 위해 개별 주문 조회
      const detailedOrders = await Promise.all(
        orderData.map(async (order: any) => {
          try {
            const detailResponse = await axiosInstance.get(
              `/orders/${order.id}`
            );
            const detailData = detailResponse.data.data;
            console.log(`=== 주문 ${order.id} 상세 데이터 ===`);
            console.log("원본 sellerId:", order.sellerId);
            console.log("상세 데이터:", detailData);
            console.log("상세 데이터 sellerId:", detailData.sellerId);
            console.log(
              "상세 데이터 product.user.id:",
              detailData.product?.user?.id
            );

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

      // API 응답을 OrderInfo 형태로 변환
      const mappedOrders = detailedOrders.map((order: any) => {
        const sellerId = order.sellerId || order.product?.user?.id || 0;
        console.log(
          `주문 ${order.id}: sellerId=${order.sellerId}, product.user.id=${order.product?.user?.id}, 최종=${sellerId}`
        );

        return {
          id: order.id,
          productName: order.product?.name || "상품명 없음",
          price: order.price || 0,
          createdAt: order.createdAt,
          sellerName:
            order.product?.nickname || order.product?.username || "판매자",
          reprImgSrc: order.product.reprImgSrc,
          sellerId: sellerId,
          status: order.status,
          auctionTitle: order.product?.name
            ? `${order.product.name} (경매 #${order.auctionId})`
            : `경매 #${order.auctionId}`,
        };
      });

      setOrders(mappedOrders);
    } catch (err: any) {
      console.error("주문 목록 조회 실패:", err);
      setError(
        err.response?.data?.message || "주문 목록을 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  // 리뷰 작성 페이지로 이동
  const handleReviewClick = (sellerId: number, sellerName: string) => {
    navigate(`/seller/detail/${sellerId}`, {
      state: {
        farmName: sellerName,
        tab: "농장 리뷰",
      },
    });
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  // 배송 상태별 스타일 반환 함수
  const getStatusStyle = (status: string) => {
    switch (status) {
      case "WAITING":
        return "text-xs text-orange-500 bg-orange-50 px-2 py-1 rounded";
      case "PAID":
        return "text-xs text-blue-500 bg-blue-50 px-2 py-1 rounded";
      case "SHIPPED":
        return "text-xs text-purple-500 bg-purple-50 px-2 py-1 rounded";
      case "COMPLETED":
        return "text-xs text-green-500 bg-green-50 px-2 py-1 rounded";
      case "CANCELLED":
        return "text-xs text-red-400 bg-red-50 px-2 py-1 rounded";
      case "REFUNDED":
        return "text-xs text-gray-500 bg-gray-50 px-2 py-1 rounded";
      case "PENDING":
        return "text-xs text-yellow-500 bg-yellow-50 px-2 py-1 rounded";
      default:
        return "text-xs text-gray-500 bg-gray-50 px-2 py-1 rounded";
    }
  };

  // 상태 한글화
  const getStatusText = (status: string) => {
    switch (status) {
      case "WAITING":
        return "결제 대기";
      case "PAID":
        return "결제 완료";
      case "SHIPPED":
        return "배송 중";
      case "COMPLETED":
        return "배송 완료";
      case "CANCELLED":
        return "취소됨";
      case "REFUNDED":
        return "환불됨";
      case "PENDING":
        return "환불 요청";
      default:
        return status;
    }
  };

  // 날짜 포맷팅
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="text-gray-500">구매 기록을 불러오는 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
        {error}
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">구매 기록</h2>
      {orders.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-500">구매 기록이 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {orders.map((order) => (
            <div
              key={order.id}
              className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
            >
              <div className="flex gap-3 items-center">
                {/* 상품 이미지 플레이스홀더 */}
                <div className="w-20 h-20 bg-gray-100 rounded-md flex items-center justify-center border border-gray-200 flex-shrink-0">
                  {order.reprImgSrc ? (
                    <img src={`data:image/jpeg;base64,${order.reprImgSrc}`} />
                  ) : (
                    <span className="text-gray-400 text-xs">이미지</span>
                  )}
                </div>

                {/* 상품 정보 */}
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                    <h3
                      className="text-base font-semibold text-slate-800 truncate leading-tight"
                      title={order.productName}
                    >
                      {order.productName}
                    </h3>
                    <span className={getStatusStyle(order.status)}>
                      {getStatusText(order.status)}
                    </span>
                  </div>

                  {order.auctionTitle && (
                    <p className="text-sm text-gray-600 truncate mb-1">
                      경매: {order.auctionTitle}
                    </p>
                  )}

                  <p className="text-sm text-gray-500 mb-1">
                    판매자: {order.sellerName}
                  </p>

                  <div className="flex items-center justify-between">
                    <p className="text-xs text-gray-400">
                      구매일: {formatDate(order.createdAt)}
                    </p>
                    <p className="text-lg font-bold text-green-600">
                      {order.price.toLocaleString()}원
                    </p>
                  </div>
                </div>
              </div>

              {/* 리뷰 작성 버튼 (배송 완료된 경우만) */}
              {order.status === "COMPLETED" && (
                <button
                  className="w-full mt-3 px-4 py-2 bg-green-500 text-white text-sm rounded hover:bg-green-600 transition-colors"
                  onClick={() =>
                    handleReviewClick(order.sellerId, order.sellerName)
                  }
                >
                  리뷰 작성
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PurchasedList;
