import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthInput from "../../../components/AuthInput";
import AuthRadioInput from "../../../components/AuthRadioInput";
import axiosInstance from "../../../api/axiosInstance";
import axios from "axios";

interface RegisterForm {
	role: string;
	id: string;
	password: string;
	nickname: string;
	email: string;
	emailId: string;
	emailDomain: string;
	phone: string;
	phoneFirst: string;
	phoneMiddle: string;
	phoneLast: string;
	address: string;
}

const Register = () => {
	const [form, setForm] = useState<RegisterForm>({
		role: "",
		id: "",
		password: "",
		nickname: "",
		email: "",
		emailId: "",
		emailDomain: "",
		phone: "",
		phoneFirst: "",
		phoneMiddle: "",
		phoneLast: "",
		address: "",
	});

	const [error, setError] = useState("");
	const navigate = useNavigate();

	const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
		const { name, value } = e.target;
		setForm((prev) => {
			const newForm = { ...prev, [name]: value };

			// 이메일 처리
			if (name === "emailId" || name === "emailDomain") {
				newForm.email = newForm.emailId && newForm.emailDomain ? `${newForm.emailId}@${newForm.emailDomain}` : "";
			}

			// 전화번호 처리
			if (name === "phoneFirst" || name === "phoneMiddle" || name === "phoneLast") {
				newForm.phone =
					newForm.phoneFirst && newForm.phoneMiddle && newForm.phoneLast
						? `${newForm.phoneFirst}${newForm.phoneMiddle}${newForm.phoneLast}`
						: "";
			}

			return newForm;
		});
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();

		// 이메일과 전화번호 데이터 통합
		const updatedForm = {
			...form,
			email: form.emailId && form.emailDomain ? `${form.emailId}@${form.emailDomain}` : form.email,
			phone:
				form.phoneFirst && form.phoneMiddle && form.phoneLast
					? `${form.phoneFirst}${form.phoneMiddle}${form.phoneLast}`
					: form.phone,
		};

		const { role, id, password, nickname, email, phone, address } = updatedForm;
		if (!role || !id || !password || !nickname || !email || !phone) {
			setError("필수 항목을 입력해주세요.");
			return;
		}

		const passwordRegEx = /^.{8,}$/;
		if (password.match(passwordRegEx) === null) {
			setError("비밀번호는 8자 이상으로 작성해 주세요.");
			return;
		}

		const phoneRegEx = /^[0-9]+$/;
		if (phone.match(phoneRegEx) === null) {
			setError("전화번호는 숫자만 입력 가능합니다.");
			return;
		}

		console.log("회원가입 시도", form);
		// axiosInstance로 REST API 서버에 POST 요청 보내기
		axiosInstance
			.post<{ message?: string }>("auth/signup", {
				role: role,
				username: id,
				password: password,
				nickname: nickname,
				email: email,
				phoneNumber: phone,
				address: address,
			})
			.then((res) => {
				if (res.status === 200) {
					setError("");
					// 회원가입 성공 시 JWT 토큰이 자동으로 Authorization 헤더에 포함되어 반환됨
					// axiosInstance의 response interceptor가 자동으로 토큰을 감지하여 저장함
					navigate("/");
				} else {
					setError(res.statusText || "통신 에러 발생. 잠시 후 다시 시도해주세요");
				}
			})
			.catch((error) => {
				if (axios.isAxiosError(error)) {
					setError(error.response?.data?.message || "통신 에러 발생. 잠시 후 다시 시도해주세요");
				} else if (error instanceof Error) {
					setError(error.message || "통신 에러 발생. 잠시 후 다시 시도해주세요");
				} else {
					setError("통신 에러 발생. 잠시 후 다시 시도해주세요");
				}
				console.log(error);
			});
	};

	return (
		<div className="flex min-h-[calc(100vh-64px)] items-center justify-center bg-gray-100">
			<div className="w-full my-5 max-w-md bg-white p-8 rounded-2xl shadow-md">
				<h2 className="text-2xl font-bold text-center">회원가입</h2>
				<p className="text-sm text-center text-red-400 mb-6">*표는 필수 항목입니다.</p>

				<div className="relative mb-4">
					<div className="w-full border-t border-gray-300" />
				</div>

				<form onSubmit={handleSubmit} className="space-y-4">
					<AuthRadioInput
						label="회원 유형"
						name="role"
						type="radio"
						value={form.role}
						onChange={handleChange}
						required
					/>
					<AuthInput
						label="아이디"
						name="id"
						value={form.id}
						placeholder="아이디를 입력해주세요"
						onChange={handleChange}
						required
					/>
					<div>
						<AuthInput
							label="비밀번호"
							name="password"
							type="password"
							value={form.password}
							placeholder="비밀번호를 입력해주세요"
							onChange={handleChange}
							required
						/>
						<span className="p-0 m-0 text-xs text-gray-500">
							비밀번호는 8자 이상 20자 이하의 영문, 숫자만 가능합니다.
						</span>
					</div>

					<AuthInput
						label="닉네임"
						name="nickname"
						value={form.nickname}
						placeholder="닉네임을 입력해주세요"
						onChange={handleChange}
						required
					/>
					<div>
						<label htmlFor="email" className="block text-sm font-medium text-gray-700">
							이메일<span className="text-red-500"> *</span>
						</label>
						<div className="mt-1 flex items-center">
							<input
								id="emailId"
								name="emailId"
								type="text"
								value={form.emailId}
								onChange={handleChange}
								required
								placeholder="이메일"
								className="p-2 w-full px-3 rounded-l-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
							/>
							<span className="p-2 text-gray-500">@</span>
							<input
								id="emailDomain"
								name="emailDomain"
								type="text"
								value={form.emailDomain}
								onChange={handleChange}
								required
								placeholder="example.com"
								className="p-2 w-full px-3 rounded-r-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
							/>
						</div>
					</div>
					<div>
						<label htmlFor="phone" className="block text-sm font-medium text-gray-700">
							전화번호<span className="text-red-500"> *</span>
						</label>
						<div className="mt-1 flex items-center">
							<input
								id="phoneFirst"
								name="phoneFirst"
								type="text"
								value={form.phoneFirst}
								onChange={handleChange}
								required
								maxLength={3}
								placeholder="010"
								className="p-2 w-1/4 px-3 rounded-l-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
							></input>
							<span className="p-2 text-gray-500">-</span>
							<input
								id="phoneMiddle"
								name="phoneMiddle"
								type="text"
								value={form.phoneMiddle}
								onChange={handleChange}
								required
								maxLength={4}
								placeholder="1234"
								className="p-2 w-1/3 px-3 border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
							/>
							<span className="p-2 text-gray-500">-</span>
							<input
								id="phoneLast"
								name="phoneLast"
								type="text"
								value={form.phoneLast}
								onChange={handleChange}
								required
								maxLength={4}
								placeholder="5678"
								className="p-2 w-1/3 px-3 rounded-r-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
							/>
						</div>
					</div>
					<AuthInput
						label="주소"
						name="address"
						value={form.address}
						placeholder="주소를 입력해주세요"
						onChange={handleChange}
					/>
					{error && <p className="text-red-500 text-sm">{error}</p>}
					<button
						type="submit"
						className="w-full py-2 px-4 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
					>
						회원가입
					</button>
				</form>
			</div>
		</div>
	);
};

export default Register;
