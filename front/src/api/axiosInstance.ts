import axios from "axios";
import { useUserStore } from "../stores/useUserStore";

const SERVER_URL = import.meta.env.VITE_SERVER_URL || "localhost:8088";
const protocol = window.location.protocol === "https:" ? "https:" : "http:";

// ✅ 모든 요청에 쿠키 포함
axios.defaults.withCredentials = true;

const axiosInstance = axios.create({
  baseURL: `${protocol}//${SERVER_URL}/api`,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터로 accessToken 주입s
axiosInstance.interceptors.request.use(
  (config) => {
    // 스토어에서 토큰 가져오기
    const token = useUserStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: 새 accessToken 받으면 저장
axiosInstance.interceptors.response.use(
  (response) => {
    const newAccessToken = response.headers["authorization"] || response.headers["Authorization"];
    if (newAccessToken && newAccessToken.startsWith("Bearer ")) {
      const token = newAccessToken.substring(7);
      // console.log("new Token: "+token);
      // 스토어에 새 토큰 저장
      useUserStore.getState().login(token);
    }
    return response;
  },
  (error) => {
    // 에러를 그대로 반환 (자동 로그아웃 처리 제거)
    return Promise.reject(error);
  }
);

// 찜 관련 API 함수들
export const bookmarkApi = {
  // 라이브 찜 추가
  addLiveBookmark: (liveId: number) => 
    axiosInstance.post(`/bookmark/live/${liveId}`),
  
  // 라이브 찜 삭제
  removeLiveBookmark: (liveId: number) => 
    axiosInstance.delete(`/bookmark/live/${liveId}`),
  
  // 찜한 라이브 목록 조회
  getLiveBookmarks: () => 
    axiosInstance.get('/bookmark/live'),
};

export default axiosInstance;
