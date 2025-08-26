import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance, { bookmarkApi } from "../../../api/axiosInstance";
import AuctionCard from "../../../components/Auction/AuctionCard";
import CountDownButton from "../../../components/Auction/CountDownButton";
import LiveStatusBadge from "../../../components/Auction/LiveStatusBadge";
import { useUserStore } from "../../../stores/useUserStore";
import dayjs from "dayjs";

interface Product {
  id: number;
  name: string;
  origin: string;
  weight: string;
  reprImgSrc: string;
  description: string;
  grade: string;
  createdAt: string;
  userId: number;
  username: string;
  categoryId: number;
  categoryName: string;
}

interface Auction {
  id: number;
  startPrice: number;
  amount: number;
  status: string;
  createdAt: string;
  product: Product;
}

interface LiveDetailData {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  liveStatus?: string; // 백엔드에서 받는 status 필드 추가
  seller: {
    sellerId: number;
    nickname: string;
  };
  auctions: Auction[];
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export default function LiveDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isLoggedIn, role } = useUserStore();
  const [live, setLive] = useState<LiveDetailData | null>(null);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // 페이지 이동시 스크롤 상단으로 고정
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    axiosInstance
      .get<ApiResponse<LiveDetailData>>(`/auction/live/${id}`)
      .then((res) => setLive(res.data.data))
      .catch((err) => console.error("❌ 라이브 상세 조회 실패:", err));
  }, [id]);

  // 찜 상태 확인
  useEffect(() => {
    if (isLoggedIn && live) {
      checkBookmarkStatus();
    }
  }, [isLoggedIn, live]);

  const checkBookmarkStatus = async () => {
    try {
      const response = await bookmarkApi.getLiveBookmarks();
      const bookmarkedLives = response.data.data || [];
      const isBookmarked = bookmarkedLives.some(
        (bookmarkedLive: { id: number }) => bookmarkedLive.id === live?.id
      );
      setIsBookmarked(isBookmarked);
    } catch (error) {
      console.error("찜 상태 확인 실패:", error);
    }
  };

  const handleBookmarkClick = async () => {
    if (!live) return;

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

  if (!live) return <p className="text-center text-gray-400">로딩 중...</p>;

  const now = dayjs();
  const start = dayjs(live.startDate);
  const end = dayjs(live.endDate);

  // 하이브리드 로직: 시간 기반 + 백엔드 ended 상태 고려
  const liveStatus = now.isBefore(start)
    ? "scheduled"
    : live.liveStatus?.toLowerCase() === "ended"
    ? "closed"
    : "active";


  const handleSellerClick = () => {
    navigate(`/seller/detail/${live.seller.sellerId}`, {
      state: {
        farmName: live.seller.nickname,
        farmId: live.seller.sellerId,
      },
    });
  };

  const handleEnterLive = () => {
    if (liveStatus === "active") {
      navigate(`/webrtc/${live.id}`);
    }
  };

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto">
      {/* 상단 라이브 정보 카드 */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6 border relative">
        {/* 찜 버튼 - 우측 상단 */}
        {role != "ROLE_SELLER" && <div className="absolute top-4 right-8">
          <button
            onClick={handleBookmarkClick}
            disabled={isLoading}
            className={`p-2 rounded-full transition-all duration-200 ${
              isBookmarked
                ? "bg-red-500 text-white hover:bg-red-600"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200 hover:text-red-500"
            } ${
              isLoading ? "opacity-50 cursor-not-allowed" : "hover:scale-110"
            }`}
            title={isBookmarked ? "찜 해제" : "찜하기"}
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
        </div>}

        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <button
                onClick={handleSellerClick}
                className="px-2 py-1 text-xs font-medium bg-green-100 text-gray-800 rounded-full hover:bg-green-500 hover:text-white transition"
              >
                {live.seller.nickname}
              </button>
              <LiveStatusBadge
                liveStatus={live.liveStatus}
                startDate={live.startDate}
                endDate={live.endDate}
              />
            </div>
            <h1 className="text-xl sm:text-2xl font-bold text-gray-900">
              {live.title}
            </h1>
            <div className="text-sm text-gray-600 mt-1">
              시작: {start.format("YYYY.MM.DD HH:mm")} <br />
              종료: {end.format("YYYY.MM.DD HH:mm")}
            </div>
          </div>

          {/* 카운트다운 버튼 */}
          <div
            onClick={handleEnterLive}
            className={`mt-4 sm:mt-0 ${
              liveStatus !== "active" ? "cursor-not-allowed opacity-60" : ""
            }`}
          >
            <CountDownButton
              auctionDate={live.startDate}
              status={liveStatus}
              auctionId={live.id}
              sellerId={live.seller.sellerId}
            />
          </div>
        </div>
      </div>

      {/* 경매 목록 제목과 구분선 */}
      <div className="mb-4">
        <h2 className="text-lg font-semibold text-gray-800">경매 목록</h2>
        <hr className="mt-2 border-gray-300" />
      </div>

      {/* 경매 목록 */}
      {/* 반응형 일단 제거 // sm:grid-cols-2 md:grid-cols-3  */}
      <div className="grid grid-cols-1 gap-6">
        {live.auctions.map((auction) => (
          <AuctionCard
            key={auction.id}
            name={auction.product.name}
            description={auction.product.description}
            auctionDate={dayjs(auction.createdAt).format("YYYY.MM.DD HH:mm")}
            category={auction.product.origin}
            badges={["무료 배송"]}
            weight={auction.product.weight}
            amount={auction.amount}
            grade={auction.product.grade}
            startPrice={auction.startPrice}
          />
        ))}
      </div>
    </div>
  );
}
