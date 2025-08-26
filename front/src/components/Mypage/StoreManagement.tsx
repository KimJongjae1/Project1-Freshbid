import { useState, useEffect } from "react";
import type { ChangeEvent } from "react";
import axiosInstance from "../../api/axiosInstance";
import { useNavigate } from "react-router-dom";

interface StoreData {
  headerImage: string | null;
  storeName: string;
  description: string;
}

const StoreManagement = () => {
  const navigate = useNavigate();
  const [storeData, setStoreData] = useState<StoreData>({
    headerImage: null,
    storeName: "",
    description: "",
  });
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  // 기존 스토어 정보 불러오기
  useEffect(() => {
    const fetchStoreData = async () => {
      try {
        setIsLoading(true);
        // API 호출 - 실제 구현 시 아래 주소를 실제 API 주소로 변경
        const response = await axiosInstance.post("/auction/product");
        setStoreData(response.data);
        setPreviewImage(response.data.headerImage);
        
        // 테스트용 더미 데이터 (실제 구현 시 제거)
        setTimeout(() => {
          const dummyData = {
            headerImage: "/default.jpg",
            storeName: "내 농장",
            description: "신선한 농산물을 판매합니다.",
          };
          setStoreData(dummyData);
          setPreviewImage(dummyData.headerImage);
          setIsLoading(false);
        }, 500);
      } catch (error) {
        console.error("스토어 정보를 불러오는데 실패했습니다.", error);
        setErrorMessage("스토어 정보를 불러오는데 실패했습니다.");
        setIsLoading(false);
      }
    };

    fetchStoreData();
  }, []);

  // 입력값 변경 핸들러
  const handleInputChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setStoreData((prev) => ({ ...prev, [name]: value }));
  };

  // 이미지 업로드 핸들러
  const handleImageUpload = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 이미지 미리보기 설정
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewImage(reader.result as string);
    };
    reader.readAsDataURL(file);

    // 실제 구현 시 FormData를 사용하여 서버에 이미지 업로드 처리
    // const formData = new FormData();
    // formData.append("image", file);
    // axios.post("/api/upload", formData)
    //   .then(response => setStoreData(prev => ({ ...prev, headerImage: response.data.imageUrl })))
    //   .catch(error => console.error("이미지 업로드 실패", error));
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setIsLoading(true);
      setSuccessMessage("");
      setErrorMessage("");

      // API 호출 - 실제 구현 시 아래 주석을 해제하고 실제 API 주소로 변경
      // await axios.post("/api/store", storeData);
      
      // 성공 처리 (테스트용 - 실제 구현 시 제거)
      setTimeout(() => {
        setSuccessMessage("스토어 정보가 성공적으로 저장되었습니다.");
        setIsLoading(false);
      }, 1000);
    } catch (error) {
      console.error("스토어 정보 저장 실패", error);
      setErrorMessage("스토어 정보를 저장하는데 실패했습니다.");
      setIsLoading(false);
    }
  };

  // 상품 추가 페이지로 이동
  const handleProductCreate = () => {
    navigate('/product/create');
  };

  // 라이브 추가 페이지로 이동
  const handleAuctionCreate = () => {
    navigate('/auction/create');
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800">스토어 관리</h2>
        <div className="flex space-x-3">
          <button
            onClick={handleProductCreate}
            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition"
          >
            판매 상품 추가
          </button>
          <button
            onClick={handleAuctionCreate}
            className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition"
          >
            라이브 추가
          </button>
        </div>
      </div>
      
      {successMessage && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {successMessage}
        </div>
      )}
      
      {errorMessage && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {errorMessage}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 헤더 이미지 업로드 */}
        <div>
          <label className="block text-gray-700 text-sm font-bold mb-2">
            스토어 헤더 이미지
          </label>
          <div className="mb-4">
            <div className="w-full h-40 bg-gray-100 overflow-hidden rounded-lg mb-2">
              {previewImage ? (
                <img 
                  src={previewImage} 
                  alt="스토어 헤더 이미지" 
                  className="w-full h-full object-cover" 
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400">
                  이미지를 업로드해 주세요
                </div>
              )}
            </div>
            <input
              type="file"
              accept="image/*"
              onChange={handleImageUpload}
              className="block w-full text-sm text-gray-500
                file:mr-4 file:py-2 file:px-4
                file:rounded-full file:border-0
                file:text-sm file:font-semibold
                file:bg-green-50 file:text-green-700
                hover:file:bg-green-100"
            />
          </div>
        </div>
        
        {/* 스토어 이름 */}
        <div>
          <label htmlFor="storeName" className="block text-gray-700 text-sm font-bold mb-2">
            스토어 이름
          </label>
          <input
            type="text"
            id="storeName"
            name="storeName"
            value={storeData.storeName}
            onChange={handleInputChange}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            required
          />
        </div>
        
        {/* 한 줄 소개 */}
        <div>
          <label htmlFor="description" className="block text-gray-700 text-sm font-bold mb-2">
            한 줄 소개
          </label>
          <textarea
            id="description"
            name="description"
            value={storeData.description}
            onChange={handleInputChange}
            rows={3}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            required
          />
        </div>
        
        {/* 저장 버튼 */}
        <div className="flex justify-end">
          <button
            type="submit"
            disabled={isLoading}
            className={`px-6 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition ${
              isLoading ? "opacity-50 cursor-not-allowed" : ""
            }`}
          >
            {isLoading ? "저장 중..." : "저장하기"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default StoreManagement;