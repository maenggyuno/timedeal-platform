// src/api/axiosConfig.js
import axios from 'axios';

// 1. 환경변수 설정
const BASE_URL = process.env.REACT_APP_BACKEND_URL || '';

// 2. Axios 인스턴스 생성
const api = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // 쿠키(RefreshToken) 주고받기 위해 필수
  headers: {
    'Content-Type': 'application/json',
  },
});

// 3. 응답 인터셉터 (기존 axiosConfig.js에 있던 재발급 로직 통합!)
api.interceptors.response.use(
  (response) => response, // 성공 시 그대로 반환
  async (error) => {
    const originalRequest = error.config;

    // 401 에러(토큰 만료) 발생 시 & 아직 재시도 안 했을 때
    if (error.response && error.response.status === 401 && !originalRequest._retry) {

      // 재발급 요청 자체가 실패한 거라면 포기 (무한 루프 방지)
      if (originalRequest.url === '/api/auth/refresh') {
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      try {
        console.log("Access token expired. Refreshing...");
        // 토큰 재발급 요청 (이 요청도 이 인스턴스를 사용하되, 무한루프 방지 조건에 걸림)
        await api.post('/api/auth/refresh');

        console.log("Token refreshed. Retrying original request.");
        // 재발급 성공 시 원래 요청 다시 시도
        return api(originalRequest);
      } catch (refreshError) {
        console.error("Refresh failed. Redirecting to login.");
        // 재발급 실패 시 로그인 페이지로 튕겨내거나 처리
        // window.location.href = '/login'; // 필요 시 주석 해제
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

// 👇 [현업 꿀팁] 요청 보낼 때마다 토큰 있으면 자동으로 헤더에 껴주는 '인터셉터'
axiosConfig.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
