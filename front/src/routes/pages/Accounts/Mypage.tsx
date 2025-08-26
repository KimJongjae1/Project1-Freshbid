import { useState, useEffect } from "react";
import PurchasedList from "../../../components/Mypage/PurchasedList";
import WishBidList from "../../../components/Mypage/WishBidList";
import WishFarmList from "../../../components/Mypage/WishFarmList";
import EditProfile from "../../../components/Mypage/EditProfile";
import CartList from "../../../components/Mypage/CartList";
import axiosInstance from "../../../api/axiosInstance";
import { useUserStore } from "../../../stores/useUserStore";

const Tabs = [
  "회원정보 수정",
  "장바구니",
  "구매 기록",
  "찜한 라이브",
  "찜한 농장",
] as const;
const SellerTabs = ["회원정보 수정"] as const;
const CustomerTabs = [
  "회원정보 수정",
  "장바구니",
  "구매 기록",
  "찜한 라이브",
  "찜한 농장",
] as const;
type TabType = (typeof Tabs)[number];

interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  phoneNumber: string;
  profileImage: string | null;
  email: string;
  address: string;
  introduction: string;
}
const MyPage = () => {
  const [activeTab, setActiveTab] = useState<TabType>("회원정보 수정");
  const [userInfo, setUserInfo] = useState<UserProfile | null>(null);

  const fetchUserInfo = async () => {
    try {
      const response = await axiosInstance.get("my-page");
      const data: UserProfile = response.data.data;
      console.log(data);
      setUserInfo(data);
    } catch (err) {
      console.error("사용자 정보 조회 실패:", err);
    }
  };

  useEffect(() => {
    window.scrollTo(0, 0);
    fetchUserInfo();
  }, []);

  return (
    <div className="max-w-4xl mx-auto px-4 py-6">
      {/* 유저 프로필 */}
      <div className="flex items-center justify-between my-6">
        <div className="flex items-center space-x-4">
          <img
            src={
              userInfo?.profileImage
                ? `data:image/jpeg;base64,${userInfo.profileImage}`
                : "/default-profile.png"
            }
            alt="사용자 프로필"
            className="w-16 h-16 rounded-full object-cover"
          />
          <div>
            <h2 className="text-xl font-bold text-gray-800">
              {userInfo?.nickname || userInfo?.username || "사용자"}님
            </h2>
            <p className="text-sm text-gray-500">안녕하세요, 반가워요!</p>
            {userInfo?.email && (
              <p className="text-xs text-gray-400">{userInfo.email}</p>
            )}
          </div>
        </div>
        {useUserStore().role === "ROLE_SELLER" && (
          <a
            href={`/seller/detail/${userInfo?.id}`}
            className="px-2 py-2 border rounded-sm border-gray-300 flex hover:border-gray-500"
          >
            <img src="favicon.png" className="w-8 h-8 mr-2" />
            <span className="my-auto">내 스토어 바로가기</span>
          </a>
        )}
      </div>

      {/* 탭 바 */}
      <div className="border-b my-4 flex justify-center">
        <ul className="flex space-x-6 text-gray-600 font-medium">
          {useUserStore().role === "ROLE_SELLER" &&
            SellerTabs.map((tabItem) => (
              <li
                key={tabItem}
                onClick={() => setActiveTab(tabItem)}
                className={`pb-2 cursor-pointer ${
                  activeTab === tabItem
                    ? "border-b-2 border-green-500 text-green-500"
                    : "hover:text-green-500"
                }`}
              >
                {tabItem}
              </li>
            ))}
          {useUserStore().role === "ROLE_CUSTOMER" &&
            CustomerTabs.map((tabItem) => (
              <li
                key={tabItem}
                onClick={() => setActiveTab(tabItem)}
                className={`pb-2 cursor-pointer ${
                  activeTab === tabItem
                    ? "border-b-2 border-green-500 text-green-500"
                    : "hover:text-green-500"
                }`}
              >
                {tabItem}
              </li>
            ))}
          {useUserStore().role === "ROLE_ADMIN" &&
            Tabs.map((tabItem) => (
              <li
                key={tabItem}
                onClick={() => setActiveTab(tabItem)}
                className={`pb-2 cursor-pointer ${
                  activeTab === tabItem
                    ? "border-b-2 border-green-500 text-green-500"
                    : "hover:text-green-500"
                }`}
              >
                {tabItem}
              </li>
            ))}
        </ul>
      </div>

      {/* 탭 내용 */}
      <div className="min-h-32 text-gray-800 text-sm">
        {activeTab === "장바구니" && <CartList />}
        {activeTab === "구매 기록" && <PurchasedList />}
        {activeTab === "찜한 라이브" && <WishBidList />}
        {activeTab === "찜한 농장" && <WishFarmList />}
        {activeTab === "회원정보 수정" && (
          <EditProfile
            userInfo={userInfo}
            onSuccess={() => setActiveTab("회원정보 수정")}
            onCancel={() => setActiveTab("회원정보 수정")}
          />
        )}
        {/* {activeTab === "스토어 관리" && <StoreManagement />} */}
      </div>
    </div>
  );
};

export default MyPage;
