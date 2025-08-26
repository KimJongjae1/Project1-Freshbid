export type AuctionStatus = "scheduled" | "active" | "closed"

export interface AuctionDetail {
  id: number
  startPrice: number
  currentPrice: number
  likeCount: number
  startTime: string
  endTime: string
  status: AuctionStatus
  product: {
    title: string
    imageUrl: string
    category: string
    deliveryDate: string
  }
  farm: {
    id: number
    name: string
  }
}
