import { useParams, useNavigate } from "react-router-dom"
import { useState, useEffect } from "react"

export default function InquiryCreate() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [title, setTitle] = useState("")
  const [content, setContent] = useState("")
  const [isPrivate, setIsPrivate] = useState(false)

  const dummyUser = {
    writer_id: 1004,
    writer_nickname: "싸피"
  }

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  const handleSubmit = () => {
    const newInquiry = {
      no: Date.now(),
      title,
      content,
      writer_id: dummyUser.writer_id,
      writer_nickname: dummyUser.writer_nickname,
      date: new Date().toISOString().slice(0, 10).replace(/-/g, "."),
      isPrivate,
      hasReply: false,
    }

    navigate(`/seller/detail/${id}`, {
      state: {
        tab: "문의",
        newInquiry,
      },
    })
  }

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">판매자 문의</h1>

      <div className="space-y-2">
        <label className="text-sm font-semibold">문의 제목*</label>
        <input
          className="w-full border rounded px-3 py-2"
          placeholder="문의 제목을 입력해주세요."
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>

      <div className="space-y-2">
        <label className="text-sm font-semibold">문의 내용*</label>
        <textarea
          className="w-full border rounded px-3 py-2 h-40 resize-none"
          placeholder="문의 내용을 입력해주세요."
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
      </div>

      <div className="space-y-2">
        <label className="text-sm font-semibold">공개 여부*</label>
        <div className="flex space-x-4">
          <label className="flex items-center space-x-1 cursor-pointer">
            <input
              type="radio"
              checked={!isPrivate}
              onChange={() => setIsPrivate(false)}
            />
            <span>공개</span>
          </label>
          <label className="flex items-center space-x-1 cursor-pointer">
            <input
              type="radio"
              checked={isPrivate}
              onChange={() => setIsPrivate(true)}
            />
            <span>비공개</span>
          </label>
        </div>
      </div>

      <div className="flex justify-end">
        <button
          onClick={handleSubmit}
          className="px-5 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          문의 등록
        </button>
      </div>
    </div>
  )
}
