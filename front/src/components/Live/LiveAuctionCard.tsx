// components/Live/LiveAuctionCard.tsx
import React from "react";
import { FaMapMarkerAlt } from "react-icons/fa";

interface AuctionCardProps {
  productName: string;
  weight: number;
  grade: string;
  origin: string;
  startPrice: number;
  amount: number;
}

const LiveAuctionCard: React.FC<AuctionCardProps> = ({
  productName,
  weight,
  grade,
  origin,
  startPrice,
  amount,
}) => {
  return (
    <div className="border border-gray-300 rounded-2xl p-4 bg-white w-full shadow-sm hover:shadow-md transition-all duration-150">
      <div className="text-xl font-semibold mb-1 flex items-center gap-2">
        {productName}
        <span className="text-green-600 text-lg font-bold">{weight}KG</span>
        <span className="text-black-600 text-lg font-bold">{amount}박스</span>
        <span className="bg-green-100 text-green-800 text-xs font-semibold px-2 py-1 rounded">
          {grade}
        </span>
      </div>
      <div className="flex items-center text-sm text-gray-600">
        <FaMapMarkerAlt className="mr-1 text-red-500" />
        <span>{origin}</span>
        <span className="mx-2">|</span>
        <span className="font-medium">시작가 {startPrice.toLocaleString()}원</span>
      </div>
    </div>
  );
};

export default LiveAuctionCard;
