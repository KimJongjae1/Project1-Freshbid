import { Navigate } from "react-router";
import { useUserStore } from "../../stores/useUserStore";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isLoggedIn } = useUserStore();

  if (!isLoggedIn) {
    // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
    return <Navigate to="/login" replace />;
  }

  // 로그인한 경우 자식 컴포넌트 렌더링
  return <>{children}</>;
};

export default ProtectedRoute;