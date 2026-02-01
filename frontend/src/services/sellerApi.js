import axios from 'axios';

//8월 11일 균오 수정 수동 재고 차감 증가
//8월 19일 절대주소 대신 상대경로로 변경
//API 호출 코드 백엔드가 만든 요리를 주문하기 위해 브라우저에서 실행될 주문서(Axios/Fetch 코드)를 작성

// .env 파일에서 URL을 가져옵니다.
// 배포 환경에서는 Nginx가 /api를 가로채므로 빈 문자열('')이거나 상대 경로일 수 있습니다.
const backendUrl = process.env.REACT_APP_BACKEND_URL || '';

const api = axios.create({
  baseURL: `${backendUrl}/api`,
  withCredentials: true,
});

// 프리사인드 URL 발급
export const getPresignedUrl = (filename, contentType) =>
    api.post('/files/presign', { filename, contentType });

// 프리사인드 URL로 S3에 직접 업로드 (PUT)
export const uploadToPresignedUrl = async (url, file, contentType) => {
    return fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': contentType },
        body: file,
    });
};

// 상품 등록 (JSON body)
export const registerProduct = (storeId, data) =>
    api.post(
        `/seller/stores/${storeId}/products`,
        data,
        { headers: { 'Content-Type': 'application/json' } }
    );

// 250809 변경: 상품 조회 (status 필터 지원: 0/1/2)
export const fetchProducts = (storeId, status) => {
    const params = (status === 0 || status === 1 || status === 2) ? { status } : {};
    return api.get(`/seller/stores/${storeId}/products`, { params });
};

// 250809 변경: 일괄 상태변경
export const bulkUpdateProductStatus = (storeId, productIds, nextStatus) =>
    api.patch(
        `/seller/stores/${storeId}/products/status`,
        { productIds, status: nextStatus },
        { headers: { 'Content-Type': 'application/json' } }
    );

// 판매 내역 조회
export const fetchSales = (storeId, status) => {
    const params = status ? { status } : {};
    return api.get(`/seller/stores/${storeId}/sales`, { params });
};

// 재고 조정(delta: +N / -N)
export const adjustProductStock = (storeId, productId, delta) =>
    api.patch(
        `/seller/stores/${storeId}/products/${productId}/stock`,
        { delta },
        { headers: { 'Content-Type': 'application/json' } }
    );

// 모든 상품의 종합 현황 데이터를 한번에 가져오는 API 함수
export const fetchProductStatusSummary = (storeId) =>
    api.get(`/seller/stores/${storeId}/products/status-summary`);
