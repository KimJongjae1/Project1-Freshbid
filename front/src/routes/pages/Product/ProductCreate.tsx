import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../api/axiosInstance";

interface ProductFormData {
	name: string;
	origin: string;
	weight: string;
	description: string;
	grade: string;
	reprImgSrc: File | null;
	categoryId: number;
}

const ProductCreate = () => {
  const navigate = useNavigate();

  const MajorCategory = [
    { value: 1, name: "채소류" },
    { value: 2, name: "과일류" },
    { value: 3, name: "견과류" },
    { value: 4, name: "곡물류" },
    { value: 5, name: "버섯류" },
    { value: 6, name: "해조류" },
  ];

  const [minorCategory, setMinorCategory] = useState<{ id: number; name: string }[]>([]);
  const grades = [
	{ id: 'special', name: '특'},
	{ id: 'high', name: '상'},
	{ id: 'middle', name: '중'},
	{ id: 'low', name: '하'}
  ]
  const [superId, setSuperId] = useState<number | string>(1);

  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSuperId(e.target.value);
  }

 useEffect(() => {
  // 대분류가 바뀌면 소분류 목록, 선택값 초기화
    setMinorCategory([]);
    setFormData(prev => ({
      ...prev,
      categoryId: 0,
    }));

    axiosInstance.get(`/categories/super/${superId}`)
      .then(res => {
        setMinorCategory(res.data.data); // 상태 업데이트
      })
      .catch(err => {
        console.error("소분류 불러오기 실패:", err);
      });
  }, [superId]); // superId 바뀔 때만 호출

  const [formData, setFormData] = useState<ProductFormData>({
    name: "",
    origin: "",
    weight: "",
    description: "",
	grade: "특",
    reprImgSrc: null,
    categoryId: 1,
  });
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "categoryId" ? Number(value) : value,
    }));
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setFormData((prev) => ({
        ...prev,
        reprImgSrc: file,
      }));

			// 이미지 미리보기
			const reader = new FileReader();
			reader.onload = () => {
				setImagePreview(reader.result as string);
			};
			reader.readAsDataURL(file);
		}
	};

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.categoryId === 0) {
      window.alert("소분류를 선택해주세요.")
      return
    }

		// TODO: API 호출로 상품 등록
		console.log("제출된 상품 정보:", formData);
		axiosInstance
			.post("/auction/product", formData, {
				headers: { "Content-Type": "multipart/form-data" }
			})
			.then((res) => {
				if (res.status === 201) {
					window.alert("상품 등록 성공!");
					// 등록 성공 후 마이페이지로 리다이렉트
					navigate(-1);
				} else {
					console.log(res);
				}
			})
			.catch((error) => {
				console.log(error);
			});
	};

	return (
		<div className="max-w-3xl mx-auto px-4 py-8">
			<h1 className="text-2xl font-bold text-center mb-8">판매 상품 등록</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
      {/* 카테고리 선택 */}
        <div className="flex gap-4">
          {/* 대분류 */}
          <div className="w-1/2">
            <label className="block text-sm font-medium mb-1">대분류</label>
            <select
              value={superId}
              onChange={handleSelectChange}
              className="w-full border px-4 py-2 border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              {MajorCategory.map(item => (
                <option key={item.value} value={item.value}>
                  {item.name}
                </option>
              ))}
            </select>
          </div>

          {/* 소분류 */}
          <div className="w-1/2">
            <label className="block text-sm font-medium mb-1">소분류</label>
            <select
              name="categoryId"
              required
              value={formData.categoryId}
              onChange={handleInputChange}
              className="w-full border border-gray-300 px-4 py-2 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              <option value={0} disabled>소분류를 선택하세요</option>
              {minorCategory.map(item => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          </div>
        </div>

				{/* 상품명 */}
				<div>
					<label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
						상품명 <span className="text-red-500">*</span>
					</label>
					<input
						type="text"
						id="name"
						name="name"
						value={formData.name}
						onChange={handleInputChange}
						required
						className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
						placeholder="상품명을 입력하세요"
					/>
				</div>

				{/* 원산지 */}
				<div>
					<label htmlFor="origin" className="block text-sm font-medium text-gray-700 mb-1">
						원산지 <span className="text-red-500">*</span>
					</label>
					<input
						type="text"
						id="origin"
						name="origin"
						value={formData.origin}
						onChange={handleInputChange}
						required
						className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
						placeholder="원산지를 입력하세요"
					/>
				</div>
				<div>
					<label htmlFor="origin" className="block text-sm font-medium text-gray-700 mb-1">
						등급 <span className="text-red-500">*</span>
					</label>
			  		<div className="flex gap-4">
						{grades.map((grade) => (
							<div>
								<input type="radio" 
									name="grade" id={grade.id} value={grade.name}
									checked={formData.grade === grade.name}
									onChange={(e) => setFormData((prev) => ({
										...prev,
										grade: e.target.value
									}))}/>
								<label htmlFor={grade.id} className="ml-2">{grade.name}</label>
							</div>
						))}
					</div>
				</div>

        {/* 무게 */}
        <div>
          <label htmlFor="weight" className="block text-sm font-medium text-gray-700 mb-1">
            무게 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="weight"
            name="weight"
            value={formData.weight}
            onChange={handleInputChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            placeholder="무게를 입력하세요. (숫자만 가능)"
          />
        </div>

				{/* 상품 설명 */}
				<div>
					<label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
						상품 설명 <span className="text-red-500">*</span>
					</label>
					<textarea
						id="description"
						name="description"
						value={formData.description}
						onChange={handleInputChange}
						required
						rows={5}
						className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
						placeholder="상품에 대한 설명을 입력하세요"
					/>
				</div>

				{/* 상품 이미지 */}
				<div>
					<label htmlFor="reprImgSrc" className="block text-sm font-medium text-gray-700 mb-1">
						상품 대표 이미지 (선택사항)
					</label>
					<input
						type="file"
						id="reprImgSrc"
						name="reprImgSrc"
						onChange={handleImageChange}
						accept="reprImgSrc/*"
						className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
					/>
					{imagePreview && (
						<div className="mt-3">
							<p className="text-sm text-gray-500 mb-1">이미지 미리보기:</p>
							<img
								src={imagePreview}
								alt="이미지 미리보기"
								className="w-40 h-40 object-cover border border-gray-300 rounded-md"
							/>
						</div>
					)}
				</div>

				{/* 등록 및 취소 버튼 */}
				<div className="flex justify-end space-x-3 pt-4">
					<button
						type="button"
						onClick={() => navigate("/mypage")}
						className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 transition"
					>
						취소
					</button>
					<button type="submit" className="px-6 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition">
						등록하기
					</button>
				</div>
			</form>
		</div>
	);
};

export default ProductCreate;