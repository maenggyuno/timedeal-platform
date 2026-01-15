import axios from 'axios';

//8월 11일 균오 수정 수동 재고 차감 증가
//8월 19일 절대주소 대신 상대경로로 변경

const api = axios.create({
    baseURL: '/api',
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
