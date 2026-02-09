//8월 11일 균오 수정 수동 재고 차감 증가
//8월 19일 절대주소 대신 상대경로로 변경
//API 호출 코드 백엔드가 만든 요리를 주문하기 위해 브라우저에서 실행될 주문서(Axios/Fetch 코드)를 작성

import api from './axiosConfig'; // ✅ 우리가 만든 설정 가져오기

// 1. 프리사인드 URL 발급
export const getPresignedUrl = (filename, contentType) =>
  api.post('/api/files/presign', { filename, contentType });

// 2. S3 업로드 (이건 백엔드 안 거치므로 fetch 유지 or axios.put 사용)
export const uploadToPresignedUrl = async (url, file, contentType) => {
  return fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': contentType },
    body: file,
  });
};

// 3. 상품 등록 (앞에 /api 명시)
export const registerProduct = (storeId, data) =>
  api.post(`/api/seller/stores/${storeId}/products`, data);

// 4. 상품 조회
export const fetchProducts = (storeId, status) => {
  const params = (status === 0 || status === 1 || status === 2) ? { status } : {};
  return api.get(`/api/seller/stores/${storeId}/products`, { params });
};

// 5. 일괄 상태변경
export const bulkUpdateProductStatus = (storeId, productIds, nextStatus) =>
  api.patch(`/api/seller/stores/${storeId}/products/status`, { productIds, status: nextStatus });

// 6. 판매 내역 조회
export const fetchSales = (storeId, status) => {
  const params = status ? { status } : {};
  return api.get(`/api/seller/stores/${storeId}/sales`, { params });
};

// 7. 재고 조정
export const adjustProductStock = (storeId, productId, delta) =>
  api.patch(`/api/seller/stores/${storeId}/products/${productId}/stock`, { delta });

// 8. 종합 현황 조회
export const fetchProductStatusSummary = (storeId) =>
  api.get(`/api/seller/stores/${storeId}/products/status-summary`);

// 9. 대시보드 매장 목록
export const fetchDashboardMarts = () =>
  api.get('/api/seller/store/dashboard');
