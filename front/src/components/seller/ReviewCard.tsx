import { useState } from "react"
import { FaStar, FaRegStar, FaEdit, FaTrash, FaReply } from "react-icons/fa"
import { useUserStore } from "../../stores/useUserStore"

// 백엔드 API 응답 타입
interface SellerReviewResponseDto {
  id: number
  userId: number
  nickname: string
  sellerId: number
  orderId: number
  content: string
  rate: number
  reviewImage?: string
  superId?: number
  createdAt: string
  updatedAt: string
  deleted: boolean
}

type ReviewCardProps = {
  review: SellerReviewResponseDto
  onUpdate: (reviewId: number, rating: number, content: string) => Promise<{ success: boolean; error?: string }>
  onDelete: (reviewId: number) => Promise<{ success: boolean; error?: string }>
  onReply?: (superId: number, content: string) => Promise<{ success: boolean; error?: string }>
}

export default function ReviewCard({
  review,
  onUpdate,
  onDelete,
  onReply,
}: ReviewCardProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isReplying, setIsReplying] = useState(false)
  const [editContent, setEditContent] = useState(review.content)
  const [editRating, setEditRating] = useState(review.rate)
  const [replyContent, setReplyContent] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState("")
  const { username, nickname } = useUserStore()

  // 현재 사용자가 리뷰 작성자인지 확인 (닉네임으로 비교)
  const isAuthor = nickname === review.nickname || username === review.nickname

  // 디버깅을 위한 로그
  console.log('ReviewCard 권한 확인:', {
    currentUser: { username, nickname },
    reviewAuthor: review.nickname,
    isAuthor,
    reviewId: review.id,
    reviewUserId: review.userId
  })

  // 날짜 포맷팅
  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

     // 수정 취소
   const handleCancelEdit = () => {
     setIsEditing(false)
     // 댓글인 경우 별점은 0으로 초기화
     setEditRating(review.superId ? 0 : review.rate)
     setEditContent(review.content)
     setError("")
   }

     // 수정 저장
   const handleSaveEdit = async () => {
     // 최상위 리뷰인 경우에만 별점 검증
     if (!review.superId && editRating === 0) {
       setError("별점을 입력해주세요")
       return
     }
     if (!editContent.trim()) {
       setError("내용을 입력해주세요")
       return
     }

     setIsSubmitting(true)
     setError("")

     // 댓글인 경우 별점은 0으로 고정
     const finalRating = review.superId ? 0 : editRating
     const result = await onUpdate(review.id, finalRating, editContent)
     
     if (result.success) {
       setIsEditing(false)
     } else {
       setError(result.error || '수정에 실패했습니다.')
     }
     
     setIsSubmitting(false)
   }

  // 삭제
  const handleDelete = async () => {
    if (!window.confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
      return
    }

    setIsSubmitting(true)
    setError("")

    const result = await onDelete(review.id)
    
    if (!result.success) {
      setError(result.error || "삭제에 실패했습니다.")
    }
    
    setIsSubmitting(false)
  }

  // 댓글 작성 시작
  const handleStartReply = () => {
    setIsReplying(true)
    setReplyContent("")
    setError("")
  }

  // 댓글 작성 취소
  const handleCancelReply = () => {
    setIsReplying(false)
    setReplyContent("")
    setError("")
  }

  // 댓글 작성 제출
  const handleSubmitReply = async () => {
    if (!replyContent.trim()) {
      setError("댓글 내용을 입력해주세요")
      return
    }

    if (!onReply) {
      setError("댓글 작성 기능이 지원되지 않습니다.")
      return
    }

    setIsSubmitting(true)
    setError("")

    const result = await onReply(review.id, replyContent)
    
    if (result.success) {
      setIsReplying(false)
      setReplyContent("")
    } else {
      setError(result.error || "댓글 작성에 실패했습니다.")
    }
    
    setIsSubmitting(false)
  }

  return (
    <div className={`border rounded-lg p-5 shadow-md bg-white space-y-4 ${
      review.superId ? 'ml-8 bg-gray-50 border-l-4 border-green-400' : ''
    } ${review.deleted ? 'opacity-60 bg-gray-100' : ''}`}>
      {/* 상단: 유저명 + 날짜 + 액션 버튼 */}
      <div className="flex justify-between items-start">
        <div>
          <p className="text-base font-semibold text-gray-800">
            {review.superId ? (
              <span className="text-green-700">💬 {review.nickname}</span>
            ) : (
              review.nickname
            )}
            {review.deleted && <span className="text-red-500 text-sm ml-2">(삭제됨)</span>}
          </p>
          {!review.superId && (
            <div className="flex items-center text-yellow-400 text-sm mt-1">
              {[...Array(5)].map((_, i) =>
                i < (isEditing ? editRating : review.rate) ? (
                  <FaStar key={i} />
                ) : (
                  <FaRegStar key={i} className="text-gray-300" />
                )
              )}
            </div>
          )}
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-xs text-gray-400 whitespace-nowrap">
            {formatDate(review.createdAt)}
          </span>
                     {isAuthor && !isEditing && !review.deleted && (
             <div className="flex space-x-1">
               <button
                 onClick={() => setIsEditing(true)}
                 className="text-blue-500 hover:text-blue-700 p-1 transition-colors"
                 title="수정"
               >
                 <FaEdit size={14} />
               </button>
               <button
                 onClick={handleDelete}
                 disabled={isSubmitting}
                 className="text-red-500 hover:text-red-700 p-1 disabled:opacity-50 transition-colors"
                 title="삭제"
               >
                 <FaTrash size={14} />
               </button>
             </div>
           )}
        </div>
      </div>

             {/* 수정 모드일 때 별점 선택 (최상위 리뷰에만) */}
       {isEditing && !review.superId && (
         <div className="flex items-center space-x-1 text-yellow-400 text-xl">
           {[...Array(5)].map((_, i) => (
             <button
               key={i}
               type="button"
               onClick={() => setEditRating(i + 1)}
               className={i < editRating ? "" : "text-gray-300"}
               disabled={isSubmitting}
             >
               ★
             </button>
           ))}
         </div>
       )}

      {/* 이미지 */}
      {review.reviewImage && (
        <div className="flex justify-center">
          <img
            src={review.reviewImage}
            alt="리뷰 이미지"
            className="w-32 h-32 object-cover rounded-md border"
          />
        </div>
      )}

      {/* 리뷰 내용 */}
      {isEditing ? (
        <div className="space-y-2">
                     <textarea
             value={editContent}
             onChange={(e) => setEditContent(e.target.value)}
             disabled={isSubmitting}
             className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm resize-none"
             rows={3}
             placeholder={review.superId ? "댓글 내용을 입력하세요" : "리뷰 내용을 입력하세요"}
           />
          {error && (
            <p className="text-red-500 text-sm">{error}</p>
          )}
          <div className="flex space-x-2">
            <button
              onClick={handleSaveEdit}
              disabled={isSubmitting}
              className="px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600 disabled:opacity-50"
            >
              {isSubmitting ? '저장 중...' : '저장'}
            </button>
            <button
              onClick={handleCancelEdit}
              disabled={isSubmitting}
              className="px-3 py-1 bg-gray-500 text-white text-sm rounded hover:bg-gray-600 disabled:opacity-50"
            >
              취소
            </button>
          </div>
        </div>
      ) : (
        <div className="flex">
          <p className="text-sm text-gray-800 text-center whitespace-pre-line max-w-[90%]">
            {review.deleted ? "삭제된 글입니다." : review.content}
          </p>
        </div>
      )}

      {/* 댓글 작성 버튼 (최상위 리뷰에만 표시, 삭제된 리뷰에는 댓글 불가) */}
      {!review.superId && !review.deleted && (
        <div className="flex justify-end">
          <button
            onClick={handleStartReply}
            className="flex items-center space-x-1 text-green-600 hover:text-green-800 text-sm transition-colors"
            title="댓글 작성"
          >
            <FaReply size={12} />
            <span>댓글</span>
          </button>
        </div>
      )}

      {/* 댓글 작성 폼 */}
      {isReplying && (
        <div className="mt-4 p-4 bg-green-50 rounded-md border border-green-200">
          <div className="space-y-3">
            <label className="block text-sm font-medium text-green-800">
              💬 댓글 작성
            </label>
            <textarea
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              disabled={isSubmitting}
              className="w-full border border-green-300 rounded-md px-3 py-2 text-sm resize-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              rows={3}
              placeholder="댓글을 입력하세요..."
              maxLength={500}
            />
            <div className="flex justify-between items-center">
              <span className="text-xs text-green-600">
                {replyContent.length}/500자
              </span>
            </div>
            {error && (
              <p className="text-red-500 text-sm">{error}</p>
            )}
            <div className="flex space-x-2">
              <button
                onClick={handleSubmitReply}
                disabled={isSubmitting || !replyContent.trim()}
                className="px-4 py-2 bg-green-500 text-white text-sm rounded-md hover:bg-green-600 disabled:opacity-50 transition-colors"
              >
                {isSubmitting ? '등록 중...' : '💬 댓글 등록'}
              </button>
              <button
                onClick={handleCancelReply}
                disabled={isSubmitting}
                className="px-4 py-2 bg-gray-500 text-white text-sm rounded-md hover:bg-gray-600 disabled:opacity-50 transition-colors"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
