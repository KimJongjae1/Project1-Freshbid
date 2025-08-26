import { Outlet } from "react-router";
import Header from "../../components/header";

export default function DefaultLayout() {
	return (
		<>
			<Header />
			<main className="min-h-[100%-64px] min-w-[500px]">
				<Outlet />
			</main>
		</>
	);
}
