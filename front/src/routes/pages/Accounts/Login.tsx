import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthInput from "../../../components/AuthInput";
import axiosInstance from "../../../api/axiosInstance";
import axios from "axios"; // axiosInstance 외에 axios도 import

interface LoginForm {
	username: string;
	password: string;
}

const Login = () => {
	const navigate = useNavigate();
	const [form, setForm] = useState<LoginForm>({ username: "", password: "" });
	const [error, setError] = useState<string>("");
	const [submitting, setSubmitting] = useState(false);

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const { name, value } = e.target;
		setForm((prev) => ({ ...prev, [name]: value }));
		if (error) setError("");
	};

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		const { username, password } = form;

		if (!username || !password) {
			setError("아이디와 비밀번호를 모두 입력해주세요.");
			return;
		}

		try {
			setSubmitting(true);
			const res = await axiosInstance.post("auth/login", { username, password });
			if (res.status === 200) {
				setError("");
				navigate("/");
				location.reload();
			} else {
				setError(res.data?.message || "로그인에 실패했어요.");
			}
		} catch (err) {
			if (axios.isAxiosError(err)) {
				setError(err.response?.data?.message || "로그인에 실패했어요.");
			} else if (err instanceof Error) {
				setError(err.message || "로그인에 실패했어요.");
			} else {
				setError("로그인에 실패했어요.");
			}
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<div className="flex min-h-screen items-center justify-center bg-gray-100">
			<div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-md">
				<h2 className="text-2xl font-bold text-center mb-6">로그인</h2>

				<div className="relative mb-4">
					<div className="w-full border-t border-gray-300" />
				</div>

				<form onSubmit={handleSubmit} className="space-y-5">
					{/* 아이디 */}
					<div>
						<label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
							아이디
						</label>
						<AuthInput
							label=""
							name="username"
							value={form.username}
							onChange={handleChange}
							placeholder="아이디를 입력해주세요"
						/>
					</div>

					{/* 비밀번호 */}
					<div>
						<label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
							비밀번호
						</label>
						<AuthInput
							label=""
							name="password"
							type="password"
							value={form.password}
							onChange={handleChange}
							placeholder="비밀번호를 입력해주세요"
						/>
						{/* 비밀번호 찾기 링크를 쓰려면 주석 해제
            <div className="flex justify-end mt-2">
              <button
                type="button"
                onClick={() => navigate("/forgot-password")}
                className="text-xs text-gray-500 hover:text-gray-700"
              >
                비밀번호를 잊으셨나요?
              </button>
            </div> */}
					</div>

					{/* 에러 메시지 */}
					{error && (
						<div
							className="flex items-start gap-2 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700"
							role="alert"
						>
							<svg className="w-4 h-4 mt-0.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
								<path
									fillRule="evenodd"
									d="M8.257 3.099c.765-1.36 2.72-1.36 3.485 0l6.52 11.596c.75 1.334-.213 3.005-1.742 3.005H3.479c-1.53 0-2.492-1.671-1.742-3.005L8.257 3.1zM11 14a1 1 0 10-2 0 1 1 0 002 0zm-1-2a1 1 0 01-1-1V8a1 1 0 112 0v3a1 1 0 01-1 1z"
									clipRule="evenodd"
								/>
							</svg>
							<span>{error}</span>
						</div>
					)}

					{/* 제출 버튼 */}
					<button
						type="submit"
						disabled={submitting}
						className={`w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg text-white font-semibold transition
              ${submitting ? "bg-green-400 cursor-not-allowed" : "bg-green-600 hover:bg-green-700"}
              focus:outline-none focus:ring-2 focus:ring-green-500/60 focus:ring-offset-2`}
					>
						{submitting && (
							<svg className="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
								<circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
								<path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
							</svg>
						)}
						로그인
					</button>

					{/* 가입 유도 */}
					<p className="text-gray-500 text-sm text-center">
						아직 회원이 아니신가요?{" "}
						<Link to="/register" className="text-green-600 hover:text-green-700 font-medium">
							회원가입하기
						</Link>
					</p>
				</form>
			</div>
		</div>
	);
};

export default Login;
