import axios from 'axios';

// 👇 [수정] 백엔드 주소 환경변수 가져오기
const backendUrl = process.env.REACT_APP_BACKEND_URL || '';

// Axios 인스턴스 생성
const loginApi = axios.create({
  // baseURL: 'http://localhost:8080',
  baseURL: backendUrl,
  withCredentials: true,
});

// 응답 인터셉터 추가
loginApi.interceptors.response.use(

  // 성공적인 응답은 그대로 반환
  (response) => {
    return response;
  },

  // 에러가 발생한 응답을 처리
  async (error) => {
    const originalRequest = error.config;

    // 401 에러이고, 아직 재시도하지 않은 요청일 경우에만 처리
    if (error.response.status === 401 && !originalRequest._retry) {

      // 무한 루프 방지 로직
      // 재발급을 시도하다가 실패한 경우 (즉, 실패한 요청이 /api/auth/refresh 였다면),
      // 더 이상 재발급을 시도하지 않고 즉시 에러를 반환
      if (originalRequest.url === '/api/auth/refresh') {
        console.error("Refresh token is invalid or expired. Redirecting to login.");
        return Promise.reject(error);
      }

      // 재시도 플래그를 true로 설정
      originalRequest._retry = true;

      try {
        console.log("Access token is expired. Attempting to refresh...");

        // 백엔드에 토큰 재발급 요청
        await loginApi.post('/api/auth/refresh');

        // 재발급 성공 시, 원래 실패했던 요청을 다시 실행
        console.log("Token refreshed successfully. Retrying the original request.");
        return loginApi(originalRequest);

      } catch (refreshError) {
        // Refresh Token마저 유효하지 않아 재발급에 최종 실패한 경우
        console.error("Could not refresh token. The user needs to log in again.", refreshError);
        return Promise.reject(refreshError);
      }
    }

    // 401 에러가 아니거나, 이미 재시도한 요청이라면 에러를 그대로 반환
    return Promise.reject(error);
  }
);

export default loginApi;
