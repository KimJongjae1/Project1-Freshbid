interface ProductCardProps {
	name: string;
	description: string;
	auctionDate: string;
	category: string;
	badges?: string[];
	weight?: string;
	imageUrl?: string;
	amount?: number;
	grade?: string;
	startPrice?: number;
}
const gradeBadgeStyles: Record<string, string> = {
	특: "bg-yellow-500 text-white",
	상: "bg-blue-500 text-white",
	중: "bg-green-500 text-white",
	하: "bg-gray-400 text-white",
};
const ProductCard: React.FC<ProductCardProps> = ({
	name,
	description,
	category,
	weight,
	imageUrl,
	amount,
	grade,
	startPrice,
}) => {
	return (
		<div style={containerStyle}>
			<div style={contentStyle}>
				<div style={titleContainerStyle}>
					<h3 style={nameStyle}>{name}</h3>
					{grade && (
						<span
							className={`text-xs font-semibold px-2 py-1 rounded-full ${
								gradeBadgeStyles[grade] || "bg-gray-300 text-gray-700"
							}`}
						>
							{grade}
						</span>
					)}
					<div style={locationContainerStyle}>
						<img
							src={imageUrl && imageUrl.trim() !== "" ? imageUrl : "/location.png"}
							alt={name}
							style={locationIconStyle}
						/>
						<span style={locationTextStyle}>{category}</span>
					</div>
				</div>
				<p style={descStyle}>{description}</p>

				{/* 무게, 개수 정보 */}
				<div style={infoContainerStyle}>
					{weight && (
						<span style={infoStyle}>
							<span style={infoLabelStyle}>무게:</span> {parseFloat(weight) % 1 === 0 ? parseInt(weight) : weight}kg
						</span>
					)}
					{amount && (
						<span style={infoStyle}>
							<span style={infoLabelStyle}>개수:</span> {amount}개
						</span>
					)}
				</div>

				{/* 시작가 정보 */}
				{startPrice && (
					<div style={priceStyle}>
						<span style={priceLabelStyle}>시작가:</span> {startPrice.toLocaleString()}원
					</div>
				)}
			</div>
		</div>
	);
};

const containerStyle: React.CSSProperties = {
	border: "1px solid #e0e0e0",
	borderRadius: "12px",
	overflow: "hidden",
	boxShadow: "0 1px 4px rgba(0, 0, 0, 0.08)",
	backgroundColor: "#fff",
	height: "auto",
};

const contentStyle: React.CSSProperties = {
	padding: "1rem",
	display: "flex",
	flexDirection: "column",
	gap: "0.5rem",
};

const nameStyle: React.CSSProperties = {
	fontSize: "18px",
	fontWeight: "bold",
	margin: 0,
	color: "#212121",
};

const descStyle: React.CSSProperties = {
	fontSize: "14px",
	color: "#555",
	lineHeight: "1.4",
};

const infoContainerStyle: React.CSSProperties = {
	display: "flex",
	alignItems: "center",
	gap: "1rem",
	flexWrap: "wrap",
	marginTop: "0.5rem",
};

const infoStyle: React.CSSProperties = {
	fontSize: "14px",
	color: "#666",
	display: "flex",
	alignItems: "center",
	gap: "0.25rem",
};

const infoLabelStyle: React.CSSProperties = {
	fontWeight: "600",
	color: "#333",
};

const priceStyle: React.CSSProperties = {
	fontSize: "16px",
	color: "#e53935",
	fontWeight: "600",
	marginTop: "0.5rem",
};

const priceLabelStyle: React.CSSProperties = {
	color: "#666",
	fontWeight: "500",
};

const titleContainerStyle: React.CSSProperties = {
	display: "flex",
	alignItems: "center",
	gap: "0.5rem",
	flexWrap: "wrap",
};

const locationContainerStyle: React.CSSProperties = {
	display: "flex",
	alignItems: "center",
	gap: "0.25rem",
};

const locationIconStyle: React.CSSProperties = {
	width: "14px",
	height: "14px",
	objectFit: "contain",
};

const locationTextStyle: React.CSSProperties = {
	fontSize: "12px",
	color: "#666",
	fontWeight: "500",
};

export default ProductCard;
