import React from 'react';
import { useLocation } from 'react-router-dom';

const TossFail = () => {
    const location = useLocation();

    // URL 쿼리 파라미터에서 에러 메시지와 코드를 추출
    const queryParams = new URLSearchParams(location.search);
    const errorMessage = queryParams.get('message') || '알 수 없는 오류가 발생했습니다.';
    const errorCode = queryParams.get('code');

    return (
        <div style={{ padding: '20px', textAlign: 'center', lineHeight: '1.6' }}>
            <h1>결제 실패</h1>
            <p><b>오류 코드:</b> {errorCode}</p>
            <p><b>오류 메시지:</b> {errorMessage}</p>
            <button
                onClick={() => window.close()} // 버튼 클릭 시 팝업 창을 닫음
                style={{ marginTop: '20px', padding: '10px 20px', cursor: 'pointer' }}
            >
                창 닫기
            </button>
        </div>
    );
};

export default TossFail;