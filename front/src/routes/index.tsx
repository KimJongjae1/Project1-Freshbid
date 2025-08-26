import { createBrowserRouter, RouterProvider } from "react-router";
import DefaultLayout from "./layouts/Default";
import Home from "./pages/Home";
import Live from "./pages/Auction/LiveAuctionView";
import Category from "./pages/Auction/CategoryAuctionView";
import Chart from "./pages/Chart";
import Login from "./pages/Accounts/Login";
import Register from "./pages/Accounts/Register";
import Mypage from "./pages/Accounts/Mypage";
import Detail from "./pages/Auction/LiveDetail";
// import LiveAuction from './pages/Live/LiveAuction'
import SellerDetail from "./pages/Seller/SellerDetail";
import InquiryDetail from "./pages/Seller/InquiryDetail";
import InquiryCreate from "./pages/Seller/InquiryCreate";
import ProductCreate from "./pages/Product/ProductCreate";
import AuctionCreate from "./pages/Product/AuctionCreate";
import LiveRoom from "./pages/Live/LiveRoom";
import ProtectedRoute from "./components/ProtectedRoute";
import NotFound from "./pages/Error/NotFound";
import SellerSearchPage from "./pages/Seller/SellerSearchPage";

const router = createBrowserRouter([
	{
		element: <DefaultLayout />,
		children: [
			{
				path: "/",
				element: <Home />,
			},
			{
				path: "/live",
				element: <Live />,
			},
			{
				path: "/category",
				element: <Category />,
			},
			{
				path: "/auction/category",
				element: <Category />,
			},
			{
				path: "/chart",
				element: <Chart />,
			},
			{
				path: "/login",
				element: <Login />,
			},
			{
				path: "/register",
				element: <Register />,
			},
			{
				path: "/mypage",
				element: <ProtectedRoute><Mypage /></ProtectedRoute>,
			},
			{
				path: "/live/detail/:id",
				element: <Detail />,
			},
			// {
			//   path: '/live/:id',
			//   element: <LiveAuction />
			// },
			{
				path: "/seller/detail/:id",
				element: <SellerDetail />,
			},
			{
				path: "/Inquiry/detail/:id",
				element: <InquiryDetail />,
			},
			{
				path: "/seller/detail/:id/inquiry/new",
				element: <InquiryCreate />,
			},
			{
				path: "/webrtc/:id",
				element: <LiveRoom />,
			},
			{
				path: "/product/create",
				element: <ProductCreate />,
			},
			{
				path: "/auction/create",
				element: <AuctionCreate />,
			},
			{
				path: "*",
				element: <NotFound />,
			},
			{
				path: "/seller/search",
				element: <SellerSearchPage />
			}
		],
	},
]);

export default function Router() {
	return <RouterProvider router={router} />;
}
