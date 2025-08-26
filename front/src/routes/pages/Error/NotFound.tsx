import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-80px)] px-6 py-12 bg-white">
      <div className="text-center">
        <h1 className="text-9xl font-bold text-primary-500">404</h1>
        <h2 className="mt-4 text-3xl font-bold tracking-tight text-gray-900">페이지를 찾을 수 없습니다</h2>
        <p className="mt-6 text-base leading-7 text-gray-600">
          요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.
        </p>
        <div className="mt-10 flex items-center justify-center gap-x-6">
          <Link
            to="/"
            className="px-4 py-2 rounded-lg transition-colors whitespace-nowrap cursor-pointer text-gray-700 hover:text-green-50 hover:bg-green-600"
          >
            홈으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
}