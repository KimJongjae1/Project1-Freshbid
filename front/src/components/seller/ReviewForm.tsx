import { useState, useEffect } from "react"
import { useParams } from "react-router-dom"
import axiosInstance from "../../api/axiosInstance"
import { useUserStore } from "../../stores/useUserStore"

type ReviewFormProps = {
  onSubmit: (rating: number, content: string, orderId?: number) => Promise<{ success: boolean; error?: string }>
}

// 주문 정보 타입
interface OrderInfo {
  id: number
  orderNumber: string
  productName: string
  createdAt: string
}

export default function ReviewForm({ onSubmit }: ReviewFormProps) {
  const { id } = useParams<{ id: string }>()
  const { username, nickname, isLoggedIn, accessToken } = useUserStore()
  const [rating, setRating] = useState(0)
  const [content, setContent] = useState("")
  const [selectedOrderId, setSelectedOrderId] = useState<number | undefined>()
  const [orders, setOrders] = useState<OrderInfo[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 사용자 상태 디버깅
  useEffect(() => {
    console.log('ReviewForm 사용자 상태:', {
      username,
      nickname,
      isLoggedIn,
      hasToken: !!accessToken,
      sellerId: id
    })
  }, [username, nickname, isLoggedIn, accessToken, id])

  // 사용자의 주문 목록 조회 (완료된 주문만)
  const fetchOrders = async () => {
    if (!id) return

    try {
      setLoading(true)
      // 실제 주문 목록 조회
      const response = await axiosInstance.get("/orders")
      const orderData = response.data.data || []
      
      console.log("전체 주문:", orderData)
      
      // COMPLETED 상태의 주문들만 필터링
      const completedOrders = orderData.filter((order: any) => order.status === "COMPLETED")
      
      console.log("완료된 주문:", completedOrders)
      console.log("현재 판매자 ID:", id)
      
      // OrderResponseDto는 단순한 구조이므로, 상세 정보를 위해 개별 주문 조회
      const detailedOrders = await Promise.all(
        completedOrders.map(async (order: any) => {
          try {
            const detailResponse = await axiosInstance.get(`/orders/${order.id}`)
            const detailData = detailResponse.data.data
            
            // OrderDetailResponseDto에는 sellerId가 없으므로 원본 데이터의 sellerId를 추가
            return {
              ...detailData,
              sellerId: order.sellerId // 원본 OrderResponseDto의 sellerId 보존
            }
          } catch (err) {
            console.error(`주문 ${order.id} 상세 조회 실패:`, err)
            return order // 상세 조회 실패 시 기본 데이터 사용
          }
        })
      )
      
      // 현재 페이지의 판매자 ID와 주문의 판매자 ID가 일치하는 주문들만 필터링
      const sellerOrders = detailedOrders.filter((order: any) => {
        // OrderResponseDto의 sellerId를 우선적으로 사용
        const orderSellerId = order.sellerId || order.product?.user?.id
        console.log(`주문 ${order.id}: sellerId=${order.sellerId}, product.user.id=${order.product?.user?.id}, 최종=${orderSellerId}, 현재페이지=${id}`)
        return orderSellerId === parseInt(id)
      })
      
      console.log("해당 판매자의 완료된 주문:", sellerOrders)
      
      // OrderInfo 형태로 변환 (삭제된 리뷰가 있는 주문도 포함)
      const mappedOrders = sellerOrders.map((order: any) => ({
        id: order.id,
        orderNumber: `ORDER-${order.id}`,
        productName: order.product?.name || "상품명 없음",
        createdAt: order.createdAt
      }))
      
      setOrders(mappedOrders)
    } catch (err: any) {
      console.error('주문 목록 조회 실패:', err)
      // 에러가 있어도 리뷰 작성은 가능하도록 함
    } finally {
      setLoading(false)
    }
  }

  // 컴포넌트 마운트 시 주문 목록 조회
  useEffect(() => {
    fetchOrders()
  }, [id])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // 로그인 상태 확인
    if (!isLoggedIn || !accessToken) {
      setError("로그인이 필요합니다.")
      return
    }
    
    if (!selectedOrderId) {
      setError("주문을 선택해주세요")
      return
    }
    
    if (rating === 0) {
      setError("별점을 선택해주세요")
      return
    }
    
    if (!content.trim()) {
      setError("리뷰 내용을 입력해주세요")
      return
    }

    setIsSubmitting(true)
    setError("")

    try {
      console.log('리뷰 작성 시도:', {
        rating,
        content: content.substring(0, 50) + '...',
        orderId: selectedOrderId,
        sellerId: id,
        user: { username, nickname }
      })
      
             const result = await onSubmit(rating, content, selectedOrderId)
       console.log('리뷰 작성 결과:', result)
       
       if (result.success) {
         // 성공 시 폼 초기화
         setRating(0)
         setContent("")
         setSelectedOrderId(undefined)
         alert("리뷰가 성공적으로 작성되었습니다!")
       } else {
         // 실패 시 경고 메시지 표시
         const errorMessage = result.error || "리뷰 작성에 실패했습니다."
         console.log('리뷰 작성 실패 에러:', errorMessage)
         
                // 구체적인 에러 메시지 처리
       if (errorMessage.includes("이미 리뷰를 작성했습니다")) {
         setError("이미 이 주문에 대한 리뷰를 작성했습니다. (삭제된 리뷰가 있는 경우 다시 작성할 수 있습니다)")
         console.log("리뷰 재작성 시도 - 백엔드에서 중복 에러 반환")
       } else if (errorMessage.includes("권한이 없습니다")) {
         setError("리뷰 작성 권한이 없습니다.")
       } else if (errorMessage.includes("로그인")) {
         setError("로그인이 필요합니다.")
       } else {
         setError(errorMessage)
       }
       }
    } catch (err: any) {
      console.error("리뷰 작성 중 오류:", err)
      setError("리뷰 작성 중 오류가 발생했습니다.")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="space-y-4 p-4 bg-gray-50 rounded-lg">
      <div className="flex justify-between items-center">
        <p className="font-semibold text-gray-700">리뷰 작성</p>
        <div className="text-xs text-gray-500">
          {isLoggedIn ? (
            <span className="text-green-600">✓ {nickname || username}님 로그인됨</span>
          ) : (
            <span className="text-red-600">✗ 로그인 필요</span>
          )}
        </div>
      </div>
      
      {/* 주문 선택 (필수) */}
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          주문 선택 <span className="text-red-500">*</span>
        </label>
        {loading ? (
          <div className="text-sm text-gray-500">주문 목록을 불러오는 중...</div>
        ) : orders.length === 0 ? (
          <div className="text-sm text-gray-500 bg-yellow-50 p-3 rounded-md">
            <p className="font-medium mb-1">⚠️ 리뷰 작성 불가</p>
            <p>리뷰를 작성할 수 있는 완료된 주문이 없습니다.</p>
            <p className="text-xs mt-1">• 완료된 주문이 있는지 확인해주세요</p>
            {/* <p className="text-xs">• 현재 판매자 ID: {id}</p> */}
          </div>
        ) : (
          <select
            value={selectedOrderId || ""}
            onChange={(e) => setSelectedOrderId(e.target.value ? parseInt(e.target.value) : undefined)}
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
            disabled={loading}
          >
            <option value="">주문을 선택하세요</option>
            {orders.map((order) => (
              <option key={order.id} value={order.id}>
                {order.productName} - {new Date(order.createdAt).toLocaleDateString()}
              </option>
            ))}
          </select>
        )}
      </div>
      
      {/* 별점 선택 */}
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          별점 <span className="text-red-500">*</span>
        </label>
        <div className="flex items-center space-x-1 text-yellow-400 text-xl">
          {[...Array(5)].map((_, i) => (
            <button
              key={i}
              type="button"
              onClick={() => setRating(i + 1)}
              disabled={isSubmitting}
              className={`${i < rating ? "" : "text-gray-300"} hover:scale-110 transition-transform disabled:opacity-50`}
            >
              ★
            </button>
          ))}
        </div>
        <p className="text-xs text-gray-500">
          {rating > 0 ? `${rating}점을 선택하셨습니다.` : "별점을 선택해주세요."}
        </p>
      </div>
      
      {/* 리뷰 내용 */}
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          리뷰 내용 <span className="text-red-500">*</span>
        </label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          disabled={isSubmitting}
          placeholder="리뷰를 작성해 주세요"
          className="w-full border border-gray-300 rounded-md px-4 py-3 text-sm resize-none"
          rows={4}
          maxLength={1000}
        />
        <div className="flex justify-between items-center">
          <span className="text-xs text-gray-500">
            {content.length}/1000자
          </span>
        </div>
      </div>
      
      {/* 제출 버튼 */}
      <div className="flex justify-end">
        <button
          onClick={handleSubmit}
          disabled={isSubmitting || rating === 0 || !content.trim() || !selectedOrderId || orders.length === 0 || !isLoggedIn}
          className="px-6 py-2 bg-green-500 text-white text-sm rounded-md hover:bg-green-600 transition disabled:opacity-50 disabled:cursor-not-allowed"
          title={
            !isLoggedIn ? "로그인이 필요합니다" :
            orders.length === 0 ? "리뷰 작성 가능한 주문이 없습니다" :
            !selectedOrderId ? "주문을 선택해주세요" :
            rating === 0 ? "별점을 선택해주세요" :
            !content.trim() ? "리뷰 내용을 입력해주세요" :
            "리뷰 등록"
          }
        >
          {isSubmitting ? "등록 중..." : "리뷰 등록"}
        </button>
      </div>
      
      {/* 에러 메시지 */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
          {error}
        </div>
      )}

      {/* 안내 메시지 */}
      <div className="text-xs text-gray-500 bg-blue-50 p-3 rounded-md">
        <p className="font-medium mb-1">💡 리뷰 작성 안내</p>
        <ul className="space-y-1">
          <li>• 구매자와 판매자만 리뷰를 작성할 수 있습니다.</li>
          <li>• 완료된 주문에 대해서만 리뷰를 작성할 수 있습니다.</li>
          <li>• 리뷰에 대한 답변은 계속해서 작성할 수 있습니다.</li>
          <li>• 본인이 작성한 리뷰만 수정/삭제할 수 있습니다.</li>
        </ul>
      </div>
    </div>
  )
}
