import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";

interface UserProfile {
	username: string;
	nickname: string;
	phoneNumber: string;
	profileImage: string | null; // Base64 문자열(헤더 없이)
	email: string;
	address: string;
	introduction: string;
}

interface EditProfileProps {
	userInfo: UserProfile | null;
	onSuccess?: () => void;
	onCancel?: () => void;
}

const EditProfile = ({ userInfo, onSuccess, onCancel }: EditProfileProps) => {
	const [form, setForm] = useState<UserProfile | null>(userInfo);
	const [loading, setLoading] = useState(false);

	// 업로드할 이미지 파일, 미리보기
	const [profileImage, setProfileImage] = useState<File | null>(null);
	const [previewImage, setPreviewImage] = useState<string | null>(null);
	const MAX_FILE_SIZE = 10 * 1024 * 1024;

	// 유저의 저장된 Base64 이미지가 유효한지 체크
	const hasProfileImage = typeof userInfo?.profileImage === "string" && userInfo.profileImage.trim().length > 0;

	// 표시용 src 계산: 미리보기 > 유저저장이미지 > 기본이미지
	const resolvedProfileSrc = previewImage
		? previewImage
		: hasProfileImage
		? `data:image/jpeg;base64,${userInfo!.profileImage}`
		: "/default-profile.png";

	useEffect(() => {
		setForm(userInfo);
	}, [userInfo]);

	const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
		if (!form) return;
		const { name, value } = e.target;
		setForm({ ...form, [name]: value });
	};

	const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (file) {
			if (file.size > MAX_FILE_SIZE) {
				alert("파일 크기는 10MB를 초과할 수 없습니다.");
				e.target.value = "";
				return;
			}
			setProfileImage(file);
			setPreviewImage(URL.createObjectURL(file)); // 미리보기 URL
		}
	};

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		if (!form) return;

		try {
			setLoading(true);

			const formData = new FormData();
			formData.append("nickname", form.nickname);
			formData.append("phoneNumber", form.phoneNumber);
			formData.append("email", form.email);
			formData.append("address", form.address);
			formData.append("introduction", form.introduction);

			if (profileImage) {
				formData.append("profileImageFile", profileImage);
			}

			await axiosInstance.put("my-page", formData, {
				headers: { "Content-Type": "multipart/form-data" },
			});

			alert("회원정보가 수정되었습니다.");
			onSuccess?.();
		} catch (err) {
			console.error("회원정보 수정 실패", err);
			alert("수정에 실패했습니다.");
		} finally {
			setLoading(false);
		}
	};

	const handleCancel = () => {
		onCancel?.();
	};

	if (!form) {
		return (
			<div className="flex justify-center items-center p-8">
				<div className="text-gray-500">회원정보를 불러오는 중...</div>
			</div>
		);
	}

	return (
		<div>
			<div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6">
				<form onSubmit={handleSubmit} className="space-y-6">
					{/* 프로필 이미지 영역 */}
					<div className="flex flex-col items-center space-y-2">
						<div className="relative w-32 h-32">
							<img
								src={resolvedProfileSrc}
								alt="프로필"
								className="w-32 h-32 rounded-full object-cover border border-gray-300"
								onError={(e) => {
									const target = e.currentTarget as HTMLImageElement;
									if (!target.src.endsWith("/default-profile.png")) {
										target.src = "/default-profile.png";
									}
								}}
							/>
							<label
								htmlFor="profileImage"
								className="absolute bottom-0 right-0 bg-green-500 text-white p-1 rounded-full cursor-pointer hover:bg-green-600 transition"
							>
								<svg
									xmlns="http://www.w3.org/2000/svg"
									className="h-5 w-5"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
								>
									<path
										strokeLinecap="round"
										strokeLinejoin="round"
										strokeWidth={2}
										d="M15.232 5.232l3.536 3.536M9 13l6-6 3 3-6 6H9v-3z"
									/>
								</svg>
							</label>
							<input type="file" id="profileImage" accept="image/*" className="hidden" onChange={handleImageChange} />
						</div>
						<p className="text-sm text-gray-500">클릭하여 프로필 사진 변경</p>
					</div>

					{/* 아이디 */}
					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">아이디 (username)</label>
						<input
							type="text"
							name="username"
							value={form.username}
							disabled
							className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500 cursor-not-allowed"
						/>
						<p className="text-sm text-gray-500">아이디는 변경할 수 없습니다</p>
					</div>

					{/* 닉네임 */}
					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">닉네임</label>
						<input
							type="text"
							name="nickname"
							value={form.nickname}
							onChange={handleChange}
							className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
						/>
					</div>

					{/* 이메일 */}
					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">이메일</label>
						<input
							type="email"
							name="email"
							value={form.email}
							onChange={handleChange}
							className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
						/>
					</div>

					{/* 휴대폰 번호 */}
					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">휴대폰 번호</label>
						<input
							type="tel"
							name="phoneNumber"
							value={form.phoneNumber}
							onChange={handleChange}
							className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
						/>
					</div>

					{/* 주소 */}
					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">주소</label>
						<input
							type="text"
							name="address"
							value={form.address}
							onChange={handleChange}
							className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
						/>
					</div>

					<div className="space-y-2">
						<label className="block text-sm font-medium text-gray-700">소개글</label>
						<textarea
							name="introduction"
							value={form.introduction}
							onChange={handleChange}
							className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
						/>
					</div>

					{/* 액션 버튼 */}
					<div className="flex justify-end space-x-3 pt-6">
						<button
							type="button"
							onClick={handleCancel}
							className="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors"
						>
							취소
						</button>
						<button
							type="submit"
							disabled={loading}
							className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:bg-green-300 disabled:cursor-not-allowed transition-colors"
						>
							{loading ? "저장 중..." : "저장"}
						</button>
					</div>
				</form>
			</div>
		</div>
	);
};

export default EditProfile;
