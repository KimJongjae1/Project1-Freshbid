import { FaReply } from "react-icons/fa";
import { useState } from "react";
import dayjs from "dayjs";

type InquiryData = {
  id: number;
  writer: WriterInfo;
  content?: string;
  parentId: number;
  createdAt: Date;
  replies: InquiryData[];
};

type WriterInfo = {
  id: number;
  username: string;
  nickname: string;
};

type InquiryCardProps = {
  isOwner: boolean;
  inquiry: InquiryData;
  onReplySubmit: (parentId: number, content: string) => void;
};

function ReplyItem({ reply }: { reply: InquiryData }) {
  return (
    <div className="ml-8 bg-orange-50 border-l-4 border-orange-300 p-4 rounded text-sm text-gray-700">
      <p className="font-semibold text-orange-800 mb-1">
        {reply.writer.nickname}{" "}
        <span className="ml-2 text-xs text-gray-500">
          {dayjs(reply.createdAt).format("YYYY.MM.DD HH:mm")}
        </span>
      </p>
      <p>{reply.content}</p>
    </div>
  );
}

function ReplyForm({
  parentId,
  onSubmit,
  onCancel,
}: {
  parentId: number;
  onSubmit: (parentId: number, content: string) => void;
  onCancel: () => void;
}) {
  const [content, setContent] = useState("");

  const handleSubmit = () => {
    if (!content.trim()) return;
    onSubmit(parentId, content);
    setContent("");
    onCancel();
  };

  return (
    <div className="ml-8 border border-gray-200 rounded p-3 bg-white">
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={3}
        className="w-full border border-gray-300 rounded p-2 text-sm focus:outline-none focus:border-orange-400"
        placeholder="답변 내용을 입력하세요..."
      />
      <div className="mt-2 flex justify-end gap-2">
        <button
          onClick={onCancel}
          className="px-3 py-1 text-xs rounded bg-gray-200 hover:bg-gray-300"
        >
          취소
        </button>
        <button
          onClick={handleSubmit}
          className="px-3 py-1 text-xs rounded bg-orange-500 text-white hover:bg-orange-600"
        >
          등록
        </button>
      </div>
    </div>
  );
}

export default function InquiryCard({
  isOwner,
  inquiry,
  onReplySubmit,
}: InquiryCardProps) {
  const [showReplies, setShowReplies] = useState(false);
  const [showReplyForm, setShowReplyForm] = useState(false);

  const toggleReplies = () => setShowReplies((prev) => !prev);

  return (
    <div className="space-y-2">
      {/* 부모글 */}
      <div className="bg-gray-50 border-l-4 border-gray-300 p-4 rounded text-sm text-gray-800 relative">
        <p className="font-semibold mb-1 flex items-center justify-between">
          <span>
            {inquiry.writer.nickname}
            <span className="ml-2 text-xs text-gray-500">
              {dayjs(inquiry.createdAt).format("YYYY.MM.DD HH:mm")}
            </span>
          </span>

          <div className="flex gap-3 items-center">
            {isOwner && (
              <button
                onClick={() => {
                  setShowReplies(true);
                  setShowReplyForm((prev) => !prev);
                }}
                className="flex items-center gap-1 text-gray-500 hover:text-gray-700 text-xs"
              >
                <FaReply className="text-sm" />
                답글 작성
              </button>
            )}
          </div>
        </p>
        <p className="text-base">{inquiry.content}</p>
        {inquiry.replies.length > 0 && (
          <button
            onClick={toggleReplies}
            className="mt-3 text-sm text-gray-500 hover:underline hover:text-black cursor-pointer"
          >
            답글 보기 ({inquiry.replies.length})
          </button>
        )}
      </div>

      {/* 답글 작성 폼 */}
      {showReplyForm && (
        <ReplyForm
          parentId={inquiry.id}
          onSubmit={onReplySubmit}
          onCancel={() => setShowReplyForm(false)}
        />
      )}

      {/* 답글 목록 */}
      {showReplies &&
        inquiry.replies.map((reply) => (
          <ReplyItem key={reply.id} reply={reply} />
        ))}
    </div>
  );
}
