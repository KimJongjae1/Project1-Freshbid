import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import LiveCard from "../../components/Auction/LiveCard";
import Carousel from "../../components/Carousel";
import LiveGuideline from "../../components/LiveGuideline";
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";

type AuctionStatus = "scheduled" | "active" | "closed";

type AuctionInLive = {
  auctionId: number;
  startPrice: number;
  currentPrice: number;
  likeCount: number;
  startTime: string;
  endTime: string;
  status?: AuctionStatus;
  product: {
    title: string;
    imageUrl: string;
    category: string;
    deliveryDate: string;
  };
  farm: {
    id: number;
    name: string;
  };
};

type LiveListItem = {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  reprImgSrc: string | null;
  liveStatus?: string; // 백엔드에서 받는 status 필드 추가
  seller: {
    sellerId: number;
    nickname: string;
  };
  auctions: AuctionInLive[];
};

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: {
    content: T[];
  };
};

const majorCategories = [
  {
    id: 1,
    name: "채소류",
    imgSrc: "/category-vegetable.png",
    color: "bg-emerald-100 hover:bg-emerald-200 border-emerald-200",
  },
  {
    id: 2,
    name: "과일류",
    imgSrc: "/category-fruit.png",
    color: "bg-red-100 hover:bg-red-200 border-red-200",
  },
  {
    id: 3,
    name: "곡물류",
    imgSrc: "/category-wheat.png",
    color: "bg-amber-100 hover:bg-amber-200 border-amber-200",
  },
  {
    id: 4,
    name: "견과류",
    imgSrc: "/category-nuts.png",
    color: "bg-orange-100 hover:bg-orange-200 border-orange-200",
  },
  {
    id: 5,
    name: "버섯류",
    imgSrc: "/category-mushroom.png",
    color: "bg-stone-100 hover:bg-stone-200 border-stone-200",
  },
  {
    id: 6,
    name: "해조류",
    imgSrc: "/category-seaweed.png",
    color: "bg-blue-100 hover:bg-blue-200 border-blue-200",
  },
];

function getStatusByTime(startTime: string, endTime: string): AuctionStatus {
  const now = dayjs();
  if (now.isBefore(dayjs(startTime))) return "scheduled";
  if (now.isAfter(dayjs(endTime))) return "closed";
  return "active";
}

// 반응형 pageSize 훅: md< -> 1, md~lg -> 2, lg<= -> 4
function useResponsivePageSize() {
  const [pageSize, setPageSize] = useState(4);

  useEffect(() => {
    const update = () => {
      const w = window.innerWidth;
      // Tailwind 기본 브레이크포인트: md=768px, lg=1024px
      if (w < 768) setPageSize(1);
      else if (w < 1024) setPageSize(2);
      else setPageSize(4);
    };
    update();
    window.addEventListener("resize", update);
    return () => window.removeEventListener("resize", update);
  }, []);

  return pageSize;
}

export default function Home() {
  const [liveList, setLiveList] = useState<LiveListItem[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const navigate = useNavigate();
  const pageSize = useResponsivePageSize();

  useEffect(() => {
    axiosInstance
      .get<ApiResponse<LiveListItem>>("/auction/live", {
        params: {
          page: 0,
          size: 4,
          sortBy: "endDate",
          sortDirection: "ASC",
          endDateFrom: new Date().toISOString().replace("Z", "").split(".")[0], // 현재 시간 이후
          statuses: ["SCHEDULED", "IN_PROGRESS"], // ENDED 상태 제외
        },
        paramsSerializer: function (params) {
          return Object.keys(params)
            .map((key) => {
              const value = params[key];
              if (Array.isArray(value)) {
                return value
                  .map((v) => `${key}=${encodeURIComponent(v)}`)
                  .join("&");
              }
              return `${key}=${encodeURIComponent(value)}`;
            })
            .join("&");
        },
      })
      .then((res) => {
        const normalized = res.data.data.content.map((live) => ({
          ...live,
          auctions: live.auctions.map((a) => ({
            ...a,
            status: getStatusByTime(a.startTime, a.endTime),
          })),
        }));
        console.log(normalized);
        setLiveList(normalized);
      })
      .catch((err) => {
        console.error("❌ 라이브 불러오기 실패:", err);
      });
  }, []);

  // 진행중인 라이브만 필터링
  const activeLives = liveList
    .map((live) => {
      const auctionsWithStatus = live.auctions.map((a) => ({
        ...a,
        status: getStatusByTime(a.startTime, a.endTime),
      }));
      return { ...live, auctions: auctionsWithStatus };
    })
    .filter((live) => {
      // ENDED 상태인 라이브는 제외
      if (live.liveStatus === "ENDED") {
        return false;
      }

      const isLiveActive =
        dayjs().isAfter(dayjs(live.startDate)) &&
        dayjs().isBefore(dayjs(live.endDate));
      return isLiveActive;
    });

  // 반응형 pageSize 적용
  const visibleLives = activeLives.slice(currentIndex, currentIndex + pageSize);
  const totalPages = Math.ceil(activeLives.length / pageSize);

  const handlePrev = () => {
    setCurrentIndex((prev) => Math.max(0, prev - pageSize));
  };

  const handleNext = () => {
    setCurrentIndex((prev) =>
      Math.min(Math.max(0, activeLives.length - pageSize), prev + pageSize)
    );
  };

  // 현재 pageSize가 바뀌면 현재 페이지가 범위를 벗어날 수 있으므로 보정
  useEffect(() => {
    setCurrentIndex((prev) => {
      const maxStart = Math.max(0, activeLives.length - pageSize);
      return Math.min(prev, maxStart);
    });
  }, [pageSize, activeLives.length]);

  const handleCategoryClick = (categoryId: number) => {
    navigate(`/auction/category?category=${categoryId}`);
  };

  return (
    <div className="px-auto space-y-16 bg-gradient-to-b from-gray-50 to-white">
      {/* 히어로 섹션 */}
      <section className="relative overflow-hidden">
        <Carousel />
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-center">
          <div className="animate-fade-in">
            <h1 className="text-4xl md:text-6xl font-bold mb-6 text-white leading-tight">
              특별한 상품을 발견하세요
            </h1>
            <p className="text-xl xl:text-2xl mb-8 text-green-100 font-light max-w-2xl mx-auto">
              신뢰할 수 있는 온라인 경매 플랫폼에서 원하는 상품을 만나보세요
            </p>
          </div>
        </div>
      </section>

      {/* 실시간 인기 경매 */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4 flex items-center justify-center gap-3">
            <span className="inline-flex items-center gap-2">
              <span className="inline-flex items-center gap-2 px-2 py-1.5 rounded-md text-xs font-semibold bg-red-100 text-red-700 align-middle">
                <span className="relative flex h-3 w-3">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-500 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-red-600"></span>
                </span>
                LIVE
              </span>
              <span>실시간 인기 경매</span>
            </span>
          </h2>

          <p className="text-gray-600 text-lg font-light">
            지금 가장 핫한 경매 상품들을 확인해보세요
          </p>
        </div>

        {activeLives.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-2xl shadow-sm ">
            <div className="w-16 h-16 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
              <svg
                className="w-8 h-8 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <p className="text-gray-500 text-lg mb-2">
              진행 중인 라이브가 없습니다
            </p>
            <p className="text-gray-400 text-sm">잠시 후 다시 확인해주세요</p>
          </div>
        ) : (
          <div className="relative">
            {/* 이전 버튼 */}
            {activeLives.length > pageSize && currentIndex > 0 && (
              <button
                onClick={handlePrev}
                className="absolute left-2.5 top-1/2 transform -translate-y-1/2 -translate-x-4 z-10 bg-white rounded-full p-3 shadow-lg hover:shadow-xl transition-all duration-200  hover:scale-105"
              >
                <svg
                  className="w-6 h-6 text-gray-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 19l-7-7 7-7"
                  />
                </svg>
              </button>
            )}

            {/* 다음 버튼 */}
            {activeLives.length > pageSize &&
              currentIndex + pageSize < activeLives.length && (
                <button
                  onClick={handleNext}
                  className="absolute right-2.5 top-1/2 transform -translate-y-1/2 translate-x-4 z-10 bg-white rounded-full p-3 shadow-lg hover:shadow-xl transition-all duration-200  hover:scale-105"
                >
                  <svg
                    className="w-6 h-6 text-gray-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </button>
              )}

            {/* 카드 그리드: 컬럼은 반응형 유지 */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mx-10">
              {visibleLives.map((live, index) => (
                <div
                  key={live.id}
                  className="animate-fade-in-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <LiveCard key={live.id} live={live} />
                </div>
              ))}
            </div>

            {/* 페이지 인디케이터 */}
            {activeLives.length > pageSize && (
              <div className="flex justify-center mt-8 space-x-3">
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => setCurrentIndex(i * pageSize)}
                    className={`w-3 h-3 rounded-full transition-all duration-300 ${
                      Math.floor(currentIndex / pageSize) === i
                        ? "bg-gray-700 scale-125 shadow-lg"
                        : "bg-gray-300 hover:bg-gray-400"
                    }`}
                  />
                ))}
              </div>
            )}
          </div>
        )}
      </section>

      {/* 경매 가이드라인 */}
      <div className="bg-gradient-to-br from-gray-50 to-white">
        <LiveGuideline />
      </div>

      {/* 실시간 경매 이용 이유 */}
      <section className="bg-gradient-to-br ">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-700 mb-6 bg-gradient-to-r bg-clip-text ">
              FreshBid를 사용해야하는 이유
            </h2>
            <p className="text-xl text-gray-600 font-light">
              거품은 내리고 상품은 올리고!
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-12 relative text-center">
            {[
              {
                icon: "🤝",
                title: "중간 과정 없이 빠르게",
                description: "실시간으로 판매자와 소비자 연결",
                gradient: "from-blue-500 to-purple-600",
              },
              {
                icon: "🥬",
                title: "신선한 먹거리",
                description: "신선도 체크 AI로 안심 구매",
                gradient: "from-green-500 to-emerald-600",
              },
              {
                icon: "🤚",
                title: "수신호 입찰",
                description: "온라인에서 생생한 경험",
                gradient: "from-orange-500 to-red-600",
              },
            ].map((feature, index) => (
              <div
                key={index}
                className="relative group"
                style={{ animationDelay: `${index * 200}ms` }}
              >
                {/* 연결선 */}
                {index < 2 && (
                  <div className="hidden md:block absolute top-1/2 -right-6 transform -translate-y-1/2 w-12 h-px bg-gradient-to-r from-gray-300 to-transparent"></div>
                )}

                <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2 border border-gray-100">
                  <div
                    className={`w-24 h-24 mx-auto mb-6 bg-gradient-to-br ${feature.gradient} rounded-2xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300`}
                  >
                    <span className="text-4xl">{feature.icon}</span>
                  </div>
                  <h3 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-gray-800">
                    {feature.title}
                  </h3>
                  <p className="text-gray-600 leading-relaxed">
                    {feature.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
      {/* 경매 보러가기 섹션 */}
      <section className="bg-gradient-to-r mb-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4 leading-tight">
              경매 시작하기
            </h2>

            <p className="text-lg text-gray-600 mb-20 max-w-xl mx-auto">
              다양한 경매에 참여하고 원하는 상품을 만나보세요
            </p>
            {/* 카테고리별 상품 */}
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 md:gap-6">
              {majorCategories.map((category, index) => (
                <div
                  key={category.id}
                  onClick={() => handleCategoryClick(category.id)}
                  className={`group flex flex-col items-center justify-center p-6 md:p-8 border-2 rounded-2xl cursor-pointer transition-all duration-300 transform hover:scale-105 hover:shadow-lg ${category.color}`}
                  style={{ animationDelay: `${index * 50}ms` }}
                >
                  <img
                    src={category.imgSrc}
                    className="w-20 h-20 mb-2"
                    alt=""
                  />
                  <h3 className="font-semibold text-gray-900 text-sm md:text-base group-hover:text-gray-800">
                    {category.name}
                  </h3>
                </div>
              ))}
            </div>
            <div className="flex flex-col mt-15 sm:flex-row gap-4 justify-center items-center max-w-md mx-auto">
              <button
                onClick={() => navigate("/live")}
                className="w-full sm:w-auto bg-green-500 text-white px-8 py-3 rounded-lg text-base font-semibold hover:bg-green-600 transition-colors duration-200 flex items-center justify-center gap-2 shadow-md hover:shadow-lg"
              >
                실시간 경매
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </button>

              <button
                onClick={() => navigate("/category")}
                className="w-full sm:w-auto bg-white border-2 border-green-300 text-green-600 px-8 py-3 rounded-lg text-base font-semibold hover:bg-green-50 hover:border-green-400 transition-all duration-200 flex items-center justify-center gap-2 shadow-md hover:shadow-lg"
              >
                카테고리별 조회
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
