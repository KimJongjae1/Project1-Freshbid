export const decodeJwtToken = (token: string) => {
  try {
    // JWT 토큰은 3부분으로 구성: header.payload.signature
    const base64Url = token.split('.')[1]; // payload 부분 추출
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/'); // URL 안전 문자 변환
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT 디코드 실패:', error);
    return null;
  }
};
