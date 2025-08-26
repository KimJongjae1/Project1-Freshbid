// src/hooks/useWsAuth.ts
import { useEffect, useRef, useState, useCallback } from "react";
import { useUserStore } from "../stores/useUserStore";

/**
 * 서버에게 "난 누구?"를 물어 JWT 기반 권한(role)을 받아오는 훅.
 * - WebSocket 연결 직후 {type:"whoami", roomId} 전송
 * - 서버는 {type:"authInfo", success, role, roomId, userId, sellerId} 응답
 * - 성공 시 role을 반환하고 소켓은 즉시 닫음(권한 핸드셰이크 전용)
 *
 * 사용 예) const { loading, role, error } = useWsAuth(roomId)
 */
type Role = "host" | "participant";

type AuthInfoSuccess = {
  type: "authInfo";
  success: true;
  roomId: number;
  role: Role;
  userId?: number;
  sellerId?: number;
};

type AuthInfoFail = {
  type: "authInfo";
  success: false;
  code?: string;       // 예: UNAUTHENTICATED | TOKEN_EXPIRED | HOST_ONLY ...
  message?: string;
};

type AuthInfo = AuthInfoSuccess | AuthInfoFail;

export function useWsAuth(roomId: number, opts?: { timeoutMs?: number }) {
  const token = useUserStore.getState().accessToken;
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState<Role | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [sellerId, setSellerId] = useState<number | null>(null);

  const wsRef = useRef<WebSocket | null>(null);
  const timerRef = useRef<number | null>(null);

  const clearTimer = () => {
    if (timerRef.current) {
      window.clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  const cleanup = () => {
    clearTimer();
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      try { wsRef.current.close(); } catch {}
    }
    wsRef.current = null;
  };

  const connect = useCallback(() => {
    // 초기화
    cleanup();
    setLoading(true);
    setRole(null);
    setError(null);
    setUserId(null);
    setSellerId(null);

    if (!roomId || Number.isNaN(roomId)) {
      setError("유효하지 않은 roomId");
      setLoading(false);
      return;
    }
    if (!token) {
      setError("토큰이 없습니다.");
      setLoading(false);
      return;
    }

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const SERVER_URL = import.meta.env.VITE_SERVER_URL; // 예: localhost:8088
    const BASE_WS = `${protocol}//${SERVER_URL}/api/call`;
    const wsUrl = `${BASE_WS}?token=${encodeURIComponent(token)}`;

    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    // 타임아웃 (기본 6초)
    const timeoutMs = opts?.timeoutMs ?? 6000;
    timerRef.current = window.setTimeout(() => {
      setError("권한 확인 타임아웃");
      setLoading(false);
      try { ws.close(4000, "auth-timeout"); } catch {}
    }, timeoutMs);

    ws.onopen = () => {
      // 권한 조회 트리거
      ws.send(JSON.stringify({ type: "whoami", roomId }));
    };

    ws.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data) as { type?: string };
        if (msg?.type !== "authInfo") {
          // 다른 메시지는 무시 (핸드셰이크 채널이지만 혹시 대비)
          return;
        }

        const info = msg as AuthInfo;
        if (info.success) {
          setRole(info.role);
          setUserId((info as AuthInfoSuccess).userId ?? null);
          setSellerId((info as AuthInfoSuccess).sellerId ?? null);
          setError(null);
        } else {
          const fail = info as AuthInfoFail;
          setError(fail.message || fail.code || "인증 실패");
          setRole(null);
        }
        setLoading(false);
        // 권한만 확인하고 닫는다 (핸드셰이크 전용)
        try { ws.close(1000, "auth-done"); } catch {}
      } catch (e) {
        setError("응답 파싱 실패");
        setLoading(false);
        try { ws.close(4001, "parse-fail"); } catch {}
      } finally {
        clearTimer();
      }
    };

    ws.onerror = () => {
      clearTimer();
      setError("웹소켓 오류");
      setLoading(false);
    };

    ws.onclose = (e) => {
      clearTimer();
      // 연결 직후 바로 닫히는 경우(1006 등) - 이미 상태 반영되었으면 무시
      if (loading && !role && !error) {
        setError(e.reason || `연결 종료(code:${e.code})`);
        setLoading(false);
      }
    };
  }, [roomId, token, opts?.timeoutMs, loading, role, error]);

  useEffect(() => {
    connect();
    return () => cleanup();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [connect]);

  const reconnect = useCallback(() => {
    connect();
  }, [connect]);

  return { loading, role, error, userId, sellerId, reconnect };
}

export default useWsAuth;
