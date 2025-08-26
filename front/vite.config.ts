import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
	plugins: [react(), tailwindcss()],
	optimizeDeps: {
		exclude: ["onnxruntime-web"], // 중요: Vite 최적화에서 제외
		include: [],
	},
	server: {
		headers: {
			"Cross-Origin-Embedder-Policy": "require-corp", //unsafe-none으로 바꾸면 minio 요청 사용 가능..
			"Cross-Origin-Opener-Policy": "same-origin",
		},
		host: true,
	},
	define: {
		global: "globalThis", // WASM 호환성
	},
	build: {
		rollupOptions: {
			output: {
				format: "es", // ES 모듈 형식 사용
			},
		},
	},
	esbuild: {
		drop: command === "build" ? ["console", "debugger"] : [],
	},
}));
