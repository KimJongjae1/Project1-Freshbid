import React from "react";

const LiveGuideline: React.FC = () => {
	return (
		<section className="bg-white">
			<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
				<div className="text-center mb-12">
					<h2 className="text-3xl font-bold text-gray-900 mb-4">경매는 이렇게 진행돼요!</h2>
					<p className="text-gray-600 text-lg">간단한 5단계로 신선한 농산물을 경매해보세요</p>
				</div>

				<div className="relative">
					{/* 세로 연결선: 모바일=왼쪽 고정, md 이상=중앙 */}
					<div
						className="
              absolute top-8 bottom-[-50px] w-0.5 bg-green-300
              left-14
              md:left-1/2 md:-translate-x-1/2
            "
					/>

					<div className="space-y-12">
						{/* 공통: 단계 블록 컴포넌트성 주석
               - 모바일: 번호가 왼쪽 라인 위에 위치(left:-1)
               - md 이상: 중앙 라인 위에 위치(left-1/2, -translate-x-1/2)
            */}

						{/* Step 1 */}
						<div className="relative">
							{/* 번호 노드 */}
							<div
								className="
                  absolute z-10 w-8 h-8 rounded-full bg-green-400 text-white text-sm font-semibold
                  flex items-center justify-center
                  left-10 top-4
                  md:left-1/2 md:-translate-x-1/2 md:top-3 /* md 이상: 중앙 정렬 */
                "
							>
								1
							</div>

							<div className="flex flex-col md:flex-row md:items-start items-center gap-8">
								<div className="flex-1 md:text-right pl-6 md:pl-0">
									{/* 모바일에서 라인과 번호 때문에 컨텐츠가 겹치지 않게 왼쪽 패딩(pl-6) 추가 */}
									<h3 className="text-xl font-bold text-gray-900 mt-3.5 md:mr-5">라이브 참여</h3>
								</div>
								<div className="flex-1 text-center md:text-left">
									<div className="flex flex-col items-center justify-center">
										<div className="flex-shrink-0 mb-3">
											<img src="/guideline/LiveClick.png" alt="라이브 참여" className="w-32 h-32 object-contain" />
										</div>
										<div className="flex-1">
											<p className="text-gray-600">원하는 라이브에 참여합니다.</p>
										</div>
									</div>
								</div>
							</div>
						</div>

						{/* Step 2 */}
						<div className="relative">
							<div
								className="
                  absolute z-10 w-8 h-8 rounded-full bg-green-400 text-white text-sm font-semibold
                  flex items-center justify-center
                  left-10 top-4
                  md:left-1/2 md:-translate-x-1/2 md:top-3
                "
							>
								2
							</div>

							<div className="flex flex-col md:flex-row-reverse md:items-start items-center gap-8">
								<div className="flex-1 md:text-left pl-6 md:pl-0">
									<h3 className="text-xl font-bold text-gray-900 mt-3.5 md:ml-5">경매 입찰</h3>
								</div>
								<div className="flex-1 text-center md:text-right md:pr-2">
									<div className="flex flex-col items-center justify-center">
										<p className="text-gray-600 text-sm mb-4">FreshBid만의 두 가지 입찰 방법으로 경매에 참여합니다.</p>

										<div className="space-y-2">
											<div className="flex justify-start p-4 bg-gray-50 rounded-lg w-64 md:ml-auto">
												<div className="flex items-center space-x-1">
													<img src="/guideline/AuctionClick.png" alt="수동 입찰" className="w-12 h-12 object-contain" />
													<div className="text-left">
														<h4 className="font-semibold text-gray-900 text-sm">수동 입찰</h4>
														<p className="text-xs text-gray-600">클릭으로 간편하게 입찰</p>
													</div>
												</div>
											</div>

											<div className="flex justify-start p-4 bg-gray-50 rounded-lg w-64 md:ml-auto">
												<div className="flex items-center space-x-1">
													<img
														src="/guideline/AIAuction.png"
														alt="AI 수신호 입찰"
														className="w-12 h-12 object-contain"
													/>
													<div className="text-left">
														<h4 className="font-semibold text-gray-900 text-sm">AI 수신호 입찰</h4>
														<p className="text-xs text-gray-600">손으로 표시만 하면 자동으로 입찰</p>
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						{/* Step 3 */}
						<div className="relative">
							<div
								className="
                  absolute z-10 w-8 h-8 rounded-full bg-green-400 text-white text-sm font-semibold
                  flex items-center justify-center
                  left-10 top-4
                  md:left-1/2 md:-translate-x-1/2 md:top-3
                "
							>
								3
							</div>

							<div className="flex flex-col md:flex-row items-center md:items-start gap-8">
								<div className="flex-1 md:text-right pl-6 md:pl-0">
									<h3 className="text-xl font-bold text-gray-900 mt-3.5 md:mr-5">낙찰</h3>
								</div>
								<div className="flex-1 text-center md:text-left">
									<div className="flex flex-col items-center justify-center">
										<div className="flex-shrink-0 mb-3">
											<img src="/guideline/Alarm.png" alt="낙찰" className="w-24 h-24 object-contain" />
										</div>
										<div className="flex-1 text-center">
											<p className="text-gray-600">낙찰 확정 시 즉시 알림하며,</p>
											<p className="text-gray-600">물품은 자동으로 장바구니로 이동합니다.</p>
										</div>
									</div>
								</div>
							</div>
						</div>

						{/* Step 4 */}
						<div className="relative">
							<div
								className="
                  absolute z-10 w-8 h-8 rounded-full bg-green-400 text-white text-sm font-semibold
                  flex items-center justify-center
                  left-10 top-4
                  md:left-1/2 md:-translate-x-1/2 md:top-3
                "
							>
								4
							</div>

							<div className="flex flex-col md:flex-row-reverse items-center md:items-start gap-8">
								<div className="flex-1 pl-6 md:pl-0">
									<h3 className="text-xl font-bold text-gray-900 mt-3.5 md:ml-5">결제</h3>
								</div>
								<div className="flex-1 text-center md:text-right md:pr-2">
									<div className="flex flex-col items-center justify-center">
										<div className="flex-shrink-0 mb-3">
											<img src="/guideline/Pay.png" alt="결제" className="w-24 h-24 object-contain" />
										</div>
										<p className="text-gray-600 text-sm mb-4">마이페이지에서 낙찰받은 물품 결제가 가능합니다.</p>
									</div>
								</div>
							</div>
						</div>

						{/* Step 5 */}
						<div className="relative">
							<div
								className="
                  absolute z-10 w-8 h-8 rounded-full bg-green-400 text-white text-sm font-semibold
                  flex items-center justify-center
                  left-10 top-4
                  md:left-1/2 md:-translate-x-1/2 md:top-3
                "
							>
								5
							</div>

							<div className="flex flex-col md:flex-row items-center md:items-start gap-8">
								<div className="flex-1 md:text-right pl-6 md:pl-0">
									<h3 className="text-xl font-bold text-gray-900 mt-3.5 md:mr-5">상품 수령</h3>
								</div>
								<div className="flex-1 text-center md:text-left">
									<div className="flex flex-col items-center justify-center">
										<div className="flex-shrink-0 mb-3">
											<img src="/guideline/Deliever.png" alt="상품 수령" className="w-24 h-24 object-contain" />
										</div>
										<div className="flex-1">
											<p className="text-gray-600">지정된 배송날짜에 신선한 농산물을 받아보실 수 있습니다.</p>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>

					{/* 마지막 연결점: 모바일에도 왼쪽, md 이상 중앙 */}
					<div
						className="
              absolute bottom-[-50px] w-3.5 h-3.5 bg-green-400 rounded-full
              left-12.5
              md:left-1/2 md:-translate-x-1/2
            "
					/>
				</div>
			</div>
		</section>
	);
};

export default LiveGuideline;
