import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';

const REDIRECT_PATH_STORAGE_KEY = 'redirectPathAfterLogin';

const BASE_URL = process.env.REACT_APP_API_URL || '';

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        const checkAuthStatusAndRedirect = async () => {
            const redirectPath = localStorage.getItem(REDIRECT_PATH_STORAGE_KEY) || '/';
            localStorage.removeItem(REDIRECT_PATH_STORAGE_KEY);

            try {
                // 백엔드에 현재 사용자 정보 요청 API 호출
                // const response = await axios.get('http://localhost:8080/api/auth/login', {
                const response = await axios.get('${BASE_URL}/api/auth/login', {
                    withCredentials: true
                });

                if (response.status === 200 && response.data) {
                    console.log("OAuth2 로그인 성공 (API 확인). 사용자 정보:", response.data);
                    console.log("이전 경로로 리다이렉트:", redirectPath);
                    navigate(redirectPath, { replace: true });
                } else {
                    throw new Error('인증되었으나 사용자 정보를 가져올 수 없습니다.');
                }
            } catch (error) {
                console.error("OAuth2 로그인 상태 확인 실패:", error.response ? error.response.data : error.message);
                let errorMessage = '로그인 처리 중 문제가 발생했습니다. 다시 시도해주세요.';

                // URL에 명시적인 에러가 있는 경우
                const urlError = searchParams.get('error');
                if (urlError) {
                    errorMessage = `로그인 실패: ${urlError}. 문제가 지속되면 관리자에게 문의하세요.`;
                } else if (error.response && error.response.status === 401) {
                    errorMessage = '인증에 실패했습니다. 다시 로그인해주세요.';
                }

                alert(errorMessage);
                const failureRedirect = redirectPath !== '/' ? redirectPath : '/login';
                navigate(failureRedirect, { replace: true });
            }
        };
        checkAuthStatusAndRedirect();
    }, [navigate, searchParams]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <p>로그인 정보를 처리 중입니다. 잠시만 기다려주세요...</p>
        </div>
    );
};

export default OAuth2RedirectHandler;
