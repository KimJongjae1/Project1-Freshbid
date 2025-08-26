import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useUserStore } from "../stores/useUserStore";
import Logo from "/logo.png";
import axiosInstance from "../api/axiosInstance";

export default function Header() {
  const [activeMenu, setActiveMenu] = useState("home");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { id: "live-auction", label: "실시간 경매", href: "/live" },
    { id: "category-auction", label: "카테고리별 경매", href: "/category" },
    { id: "price-chart", label: "낙찰가차트", href: "/chart" },
    { id: "seller-info", label: "판매자 조회", href: "/seller/search" },
  ];

  const profileImageUrl = useUserStore((s) => s.profileImage);
  const nickname = useUserStore((s) => s.nickname);
  const accessToken = useUserStore((s) => s.accessToken);
  const isLoggedIn = useUserStore((s) => s.isLoggedIn);
  const Logout = useUserStore((s) => s.logout);
  const hasProfile =
    typeof profileImageUrl === "string" && profileImageUrl.trim().length > 0;

  const [dropDownOpen, setDropDownOpen] = useState(false);
  const toggleDropDown = () => setDropDownOpen((prev) => !prev);
  const toggleMobileMenu = () => setIsMobileMenuOpen((prev) => !prev);

  // 기존 useEffect들...
  useEffect(() => {
    const fetchUserHeader = async () => {
      try {
        const response = await axiosInstance.get(`/my-page/header`);
        useUserStore.getState().setUserInfo(response.data.data);
      } catch (error) {
        console.error(error);
      }
    };
    if (isLoggedIn) fetchUserHeader();
  }, [accessToken, isLoggedIn]);

  useEffect(() => {
    const { pathname } = location;
    const match =
      menuItems.find((item) => pathname === item.href) ||
      menuItems.find((item) => pathname.startsWith(item.href + "/")) ||
      menuItems.find((item) => pathname.startsWith(item.href + "?")) ||
      menuItems.find((item) => pathname.startsWith(item.href + "#"));

    if (match) setActiveMenu(match.id);
    else setActiveMenu("");
  }, [location.pathname]);

  const handleMenuClick = (item: any) => {
    if (item.id === "seller-info") {
      if (!isLoggedIn) {
        alert("로그인 후 이용해주세요.");
        return;
      }
    }
    setActiveMenu(item.id);
    navigate(item.href);
    setIsMobileMenuOpen(false);
  };

  return (
    <header className="bg-white shadow-md border-b sticky top-0 z-50 h-[64px]  min-w-[500px] w-full mx-0">
      <div className="max-w-7xl mx-auto px-2 sm:px-4 ">
        <div className="flex justify-between items-center h-16">
          {/* 모바일: 햄버거 메뉴 (왼쪽) */}
          <div className="md:hidden flex items-center">
            <button
              onClick={toggleMobileMenu}
              className="p-2 rounded-md text-gray-700 hover:bg-gray-100"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d={
                    isMobileMenuOpen
                      ? "M6 18L18 6M6 6l12 12"
                      : "M4 6h16M4 12h16M4 18h16"
                  }
                />
              </svg>
            </button>
          </div>

          {/* 로고 (모바일: 중앙, 데스크톱: 왼쪽) */}
          <div
            className="md:flex-none absolute md:relative left-1/2 md:left-auto transform -translate-x-1/2 md:-translate-x-0 md:transform-none"
            onClick={() => navigate("/")}
          >
            <img src={Logo} className="w-full h-12 cursor-pointer" alt="Logo" />
          </div>

          {/* 데스크톱 네비게이션 메뉴 */}
          <nav className="hidden md:flex gap-4 xl:gap-6 mx-4">
            {menuItems.map((item) => (
              <button
                key={item.id}
                onClick={() => handleMenuClick(item)}
                className={`px-3 py-2 rounded-lg transition-colors whitespace-nowrap cursor-pointer text-sm xl:text-base ${
                  activeMenu === item.id
                    ? "bg-green-400 text-white"
                    : "text-gray-700 hover:text-green-600 hover:bg-green-50"
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>

          {/* 사용자 메뉴 (오른쪽 고정) */}
          <div className="flex items-center">
            {isLoggedIn ? (
              <div className="relative inline-block text-left">
                <button
                  onClick={toggleDropDown}
                  className="flex items-center gap-2 focus:outline-none hover:bg-gray-100 p-2 rounded-md cursor-pointer"
                >
                  <img
                    src={
                      hasProfile
                        ? `data:image/jpeg;base64,${profileImageUrl}`
                        : "/default-profile.png"
                    }
                    alt="프로필"
                    className="w-8 h-8 rounded-full object-cover"
                  />
                  <span className=" text-sm font-medium">
                    <span className="text-blue-500 font-bold">{nickname}</span>{" "}
                    님
                  </span>
                </button>

                {dropDownOpen && (
                  <div className="absolute right-0 mt-2 w-36 bg-white border border-gray-200 rounded-md shadow-lg z-50">
                    <ul className="py-1 text-sm">
                      <li>
                        <button
                          onClick={() => {
                            toggleDropDown();
                            navigate("/mypage");
                          }}
                          className="w-full text-left px-4 py-2 hover:bg-gray-100 cursor-pointer"
                        >
                          마이페이지
                        </button>
                      </li>
                      <li>
                        <button
                          onClick={() => {
                            Logout();
                            window.alert("성공적으로 로그아웃 하였습니다.");
                            navigate("/");
                          }}
                          className="w-full text-left px-4 py-2 text-red-500 hover:bg-gray-100 cursor-pointer"
                        >
                          로그아웃
                        </button>
                      </li>
                    </ul>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex gap-2">
                <button
                  onClick={() => navigate("/login")}
                  className="text-gray-700 px-3 py-2 rounded-lg hover:text-green-600 hover:bg-green-50 font-medium transition duration-200 text-sm"
                >
                  로그인
                </button>
                <button
                  onClick={() => navigate("/register")}
                  className="hidden sm:block text-gray-700 px-3 py-2 rounded-lg hover:text-green-600 hover:bg-green-50 font-medium transition duration-200 text-sm"
                >
                  회원가입
                </button>
              </div>
            )}
          </div>
        </div>

        {/* 모바일 네비게이션 메뉴 */}
        {isMobileMenuOpen && (
          <div className="md:hidden absolute top-full left-0 w-full bg-white border-b shadow-lg">
            <nav className="px-4 py-2 space-y-1">
              {menuItems.map((item) => (
                <button
                  key={item.id}
                  onClick={() => handleMenuClick(item)}
                  className={`w-full text-left px-4 py-3 rounded-lg transition-colors ${
                    activeMenu === item.id
                      ? "bg-green-400 text-white"
                      : "text-gray-700 hover:text-green-600 hover:bg-green-50"
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
}
