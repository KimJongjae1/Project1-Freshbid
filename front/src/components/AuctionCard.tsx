// components/AuctionCard.tsx
// import React from 'react'

interface AuctionCardProps {
	product: {
		name: string;
		origin: string;
		weight: number;
		description: string;
		grade: string;
	};
}

export default function AuctionCard({ product }: AuctionCardProps) {
	return (
		<div className="bg-white rounded-2xl shadow p-4 w-full max-w-sm border border-gray-100">
			<img src="/images/auction-sample.jpg" alt={product.name} className="w-full h-52 object-cover rounded-xl mb-4" />
			<h2 className="text-xl font-semibold text-gray-800 mb-1">{product.name}</h2>
			<p className="text-sm text-gray-500 mb-1">원산지: {product.origin}</p>
			<p className="text-sm text-gray-500 mb-1">중량: {product.weight}kg</p>
			<p className="text-sm text-gray-500 mb-1">등급: {product.grade}</p>
			<p className="text-sm text-gray-600 mt-2 whitespace-pre-line leading-relaxed">{product.description}</p>
		</div>
	);
}
