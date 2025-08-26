// components/Live/AuctionList.tsx
import React from "react";
import LiveAuctionCard from "./LiveAuctionCard";

interface Auction {
  id: number;
  startPrice: number;
  amount: number;
  status: string;
  createdAt: string;
  product: {
    name: string;
    origin: string;
    weight: string;
    grade: string;
  };
}

interface AuctionListProps {
  auctions: Auction[];
}

const AuctionList: React.FC<AuctionListProps> = ({ auctions }) => {
  return (
    <div style={{ 
      minHeight: '300px',
      maxHeight: "300px", 
      overflowY: "auto"
    }}>
      <div className="flex flex-col gap-4">
        {auctions.map((auction) => (
          <LiveAuctionCard
            key={auction.id}
            productName={auction.product.name}
            weight={Number(auction.product.weight)}
            amount={auction.amount}
            grade={auction.product.grade}
            origin={auction.product.origin}
            startPrice={auction.startPrice}
          />
        ))}
      </div>
    </div>
  );
};

export default AuctionList;
