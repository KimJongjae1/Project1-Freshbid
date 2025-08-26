// components/Mypage/WishBidList.tsx

import { useState, useEffect } from "react";
import axiosInstance from "../../api/axiosInstance";
import WishLiveCard from "./WishLiveCard";

interface WishBidListItem {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  liveStatus?: string;
  reprImgSrc: string | null;
  seller: {
    sellerId: number;
    nickname: string;
  };
  auctions: {
    product: {
      imageUrl: string;
    };
  }[];
}

const WishBidList = () => {
  const [favorites, setFavorites] = useState<WishBidListItem[]>([]);
  const [loading, setLoading] = useState(true);

  // API에서 찜 목록 가져오기
  const fetchWishlist = async () => {
    try {
      setLoading(true);
      const response = await axiosInstance.get("bookmark/live");
      const datas = response.data.data;

      console.log(datas);
      // API 응답을 LiveCard props 형태로 변환
      const mappedData = datas.map((auc: any) => {
        return {
          id: auc.id,
          title: auc.title,
          startDate: auc.startDate,
          endDate: auc.endDate,
          liveStatus: auc.status,
          reprImgSrc: auc.reprImgSrc,
          seller: auc.seller || {
            sellerId: auc.sellerId || 0,
            nickname: auc.sellerName || "판매자",
          },
          auctions: auc.auctions || [],
        };
      });

      // 데이터 정렬: 진행중 -> 시작전 -> 종료된 것 순서
      const sortedData = mappedData.sort(
        (a: WishBidListItem, b: WishBidListItem) => {
          const now = new Date();
          const aStart = new Date(a.startDate);
          const aEnd = new Date(a.endDate);
          const bStart = new Date(b.startDate);
          const bEnd = new Date(b.endDate);

          // 상태 우선순위: 진행중(1) > 시작전(2) > 종료됨(3)
          const getStatusPriority = (start: Date, end: Date) => {
            if (now >= start && now <= end) return 1; // 진행중
            if (now < start) return 2; // 시작전
            return 3; // 종료됨
          };

          const aPriority = getStatusPriority(aStart, aEnd);
          const bPriority = getStatusPriority(bStart, bEnd);

          if (aPriority !== bPriority) {
            return aPriority - bPriority;
          }

          // 같은 상태면 최신순 (시작일 기준 내림차순)
          return (
            new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
          );
        }
      );

      setFavorites(sortedData);
    } catch (err) {
      console.error("찜 목록 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWishlist();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="text-gray-500">찜 목록을 불러오는 중...</div>
      </div>
    );
  }

  if (favorites.length === 0) {
    return (
      <div>
        <h2 className="text-xl font-semibold mb-4">찜한 라이브</h2>
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-8 text-center">
          <div className="text-gray-500">찜한 라이브가 없습니다.</div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">찜한 라이브</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {favorites.map((item) => (
          <div key={item.id}>
            <WishLiveCard live={item} />
          </div>
        ))}
      </div>
    </div>
  );
};

export default WishBidList;
