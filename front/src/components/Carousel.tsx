import { Component } from "react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";

export default class SimpleSlider extends Component {
	render() {
		const settings = {
			dots: true,
			infinite: true,
			autoplay: true,
			autoplaySpeed: 3000,
			speed: 500,
			slidesToShow: 1,
			slidesToScroll: 1,
			adaptiveHeight: false,
			appendDots: (dots: React.ReactNode) => (
				<div className="pointer-events-none absolute left-1/2 z-20 w-full">
					<ul className="pointer-events-auto">{dots}</ul>
				</div>
			),
		};

		const images = ["img/01.jpg", "img/02.jpg", "img/03.jpg", "img/04.jpg"];

		return (
			<div className="relative">
				<Slider
					{...settings}
					className="
            relative
            [&_.slick-list]:m-0 [&_.slick-list]:p-0
            [&_.slick-track]:m-0 [&_.slick-track]:p-0
            [&_.slick-slide>div]:h-full
            [&_.slick-slide>div]:block
            [&_.slick-dots]:!static
          "
				>
					{images.map((src, idx) => (
						<div key={idx} className="relative w-full h-96 overflow-hidden">
							<img
								src={src}
								alt={`slide ${idx + 1}`}
								className="block w-full h-full object-cover object-center brightness-50 select-none"
							/>
						</div>
					))}
				</Slider>
			</div>
		);
	}
}
