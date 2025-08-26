import dayjs from "dayjs";

type AuctionStatus = "scheduled" | "active" | "closed";

type LiveStatusBadgeProps = {
	liveStatus?: string;
	startDate: string;
	endDate: string;
};

export default function LiveStatusBadge({ liveStatus, startDate }: LiveStatusBadgeProps) {
	const now = dayjs();
	const start = dayjs(startDate);

	// 하이브리드 로직: 시간 기반 + 백엔드 ended 상태 고려
	const status: AuctionStatus = now.isBefore(start)
		? "scheduled"
		: liveStatus?.toLowerCase() === "ended"
		? "closed"
		: "active";

	const statusLabel = {
		scheduled: "예정",
		active: "진행중",
		closed: "완료",
	}[status];

	const statusColor = {
		scheduled: "bg-gray-400",
		active: "bg-red-500",
		closed: "bg-green-500",
	}[status];

	return (
		<span className={`px-2 py-0.5 text-xs font-medium text-white rounded-full ${statusColor}`}>{statusLabel}</span>
	);
}
