// components/Mypage/WishLiveCard.tsx
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import dayjs from "dayjs";
import CountDownButton from "../Auction/CountDownButton";
import LiveStatusBadge from "../Auction/LiveStatusBadge";
import { bookmarkApi } from "../../api/axiosInstance";
import { useUserStore } from "../../stores/useUserStore";

type AuctionStatus = "scheduled" | "active" | "closed";

type WishLiveProps = {
  live: {
    id: number;
    title: string;
    startDate: string;
    endDate: string;
    liveStatus?: string;
    reprImgSrc: string | null;
    seller?: {
      sellerId: number;
      nickname: string;
    };
    auctions?: {
      product: {
        imageUrl: string;
      };
    }[];
  };
};

export default function WishLiveCard({ live }: WishLiveProps) {
  const navigate = useNavigate();
  const { isLoggedIn, role } = useUserStore();
  const [isBookmarked, setIsBookmarked] = useState(true); // 찜 목록이므로 기본값 true
  const [isLoading, setIsLoading] = useState(false);

  const thumb = live.reprImgSrc
    ? `data:image/jpeg;base64,${live.reprImgSrc}`
    : "/default.jpg";

  // 컴포넌트 마운트 시 찜 상태 확인
  useEffect(() => {
    if (isLoggedIn) {
      checkBookmarkStatus();
    }
  }, [isLoggedIn, live.id]);

  const checkBookmarkStatus = async () => {
    try {
      const response = await bookmarkApi.getLiveBookmarks();
      const bookmarkedLives = response.data.data || [];
      const isBookmarked = bookmarkedLives.some(
        (bookmarkedLive: any) => bookmarkedLive.id === live.id
      );
      setIsBookmarked(isBookmarked);
    } catch (error) {
      console.error("찜 상태 확인 실패:", error);
    }
  };

  const handleBookmarkClick = async (e: React.MouseEvent) => {
    e.stopPropagation();

    if (!isLoggedIn) {
      alert("로그인이 필요한 서비스입니다.");
      return;
    }

    if (isLoading) return;

    setIsLoading(true);
    try {
      if (isBookmarked) {
        await bookmarkApi.removeLiveBookmark(live.id);
        setIsBookmarked(false);
      } else {
        await bookmarkApi.addLiveBookmark(live.id);
        setIsBookmarked(true);
      }
    } catch (error) {
      console.error("찜 처리 실패:", error);
      alert("찜 처리 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleCardClick = () => {
    navigate(`/live/detail/${live.id}`);
  };

  const now = dayjs();
  const start = dayjs(live.startDate);
  const end = dayjs(live.endDate);

  // live.liveStatus가 있으면 서버 상태 사용, 없으면 시간 기준 계산 (fallback)
  const liveStatus: AuctionStatus = live.liveStatus
    ? live.liveStatus.toLowerCase() === "scheduled"
      ? "scheduled"
      : live.liveStatus.toLowerCase() === "in_progress"
      ? "active"
      : live.liveStatus.toLowerCase() === "ended"
      ? "closed"
      : "closed"
    : now.isBefore(start)
    ? "scheduled"
    : now.isAfter(end)
    ? "closed"
    : "active";

  return (
    <div
      className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow overflow-hidden w-full cursor-pointer relative"
      onClick={handleCardClick}
    >
      {/* 찜 버튼 - 우측 상단 */}
      {(role === "ROLE_CUSTOMER" || role === null) && (
        <div className="absolute top-2 right-2 z-10">
          <button
            onClick={handleBookmarkClick}
            disabled={isLoading}
            className={`p-2 rounded-full transition-all duration-200 ${
              isBookmarked
                ? "bg-red-500 text-white hover:bg-red-600"
                : "bg-white/80 text-gray-600 hover:bg-white hover:text-red-500"
            } ${
              isLoading ? "opacity-50 cursor-not-allowed" : "hover:scale-110"
            }`}
          >
            {isBookmarked ? (
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" />
              </svg>
            ) : (
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                />
              </svg>
            )}
          </button>
        </div>
      )}

      <div className="w-full h-48 bg-gray-100">
        <img
          src={thumb}
          onError={(e) => {
            e.currentTarget.src = "/default.jpg";
          }}
          alt={live.title}
          className="w-full h-full object-cover object-top"
        />
      </div>

      <div className="p-4 space-y-3">
        <div className="flex justify-between items-start">
          <div className="flex items-center space-x-2">
            <LiveStatusBadge
              liveStatus={live.liveStatus}
              startDate={live.startDate}
              endDate={live.endDate}
            />
          </div>
        </div>

        <h3 className="font-semibold text-lg text-gray-900 line-clamp-2 leading-tight">
          {live.title}
        </h3>

        <div className="text-sm text-gray-600 space-y-1">
          <div className="flex justify-between">
            <span>시작</span>
            <span className="text-gray-900 font-medium">
              {dayjs(live.startDate).format("YYYY.MM.DD HH:mm")}
            </span>
          </div>
          <div className="flex justify-between">
            <span>종료</span>
            <span className="text-gray-900 font-medium">
              {dayjs(live.endDate).format("YYYY.MM.DD HH:mm")}
            </span>
          </div>
        </div>

        <div
          className={
            liveStatus !== "active" ? "cursor-not-allowed opacity-60" : ""
          }
        >
          <CountDownButton
            auctionDate={live.startDate}
            status={liveStatus}
            auctionId={live.id}
            sellerId={live.seller?.sellerId}
          />
        </div>
      </div>
    </div>
  );
}
