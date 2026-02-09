import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
// 👇 [수정] axios 제거하고 api 임포트
import api from '../services/axiosConfig';

const REDIRECT_PATH_STORAGE_KEY = 'redirectPathAfterLogin';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const checkAuthStatusAndRedirect = async () => {
      const redirectPath = localStorage.getItem(REDIRECT_PATH_STORAGE_KEY) || '/';
      localStorage.removeItem(REDIRECT_PATH_STORAGE_KEY);

      try {
        // 👇 [수정] 복잡한 주소 조합 삭제 -> 깔끔하게 api.get 호출!
        // withCredentials도 axiosConfig에 이미 들어있으니 생략 가능
        const response = await api.get('/api/auth/login');

        if (response.status === 200 && response.data) {
          console.log("OAuth2 로그인 성공:", response.data);
          navigate(redirectPath, { replace: true });
        } else {
          throw new Error('인증되었으나 사용자 정보를 가져올 수 없습니다.');
        }
      } catch (error) {
        console.error("OAuth2 로그인 상태 확인 실패:", error);

        let errorMessage = '로그인 처리 중 문제가 발생했습니다.';
        const urlError = searchParams.get('error');

        if (urlError) {
          errorMessage = `로그인 실패: ${urlError}`;
        } else if (error.response && error.response.status === 401) {
          errorMessage = '인증에 실패했습니다. 다시 로그인해주세요.';
        }

        alert(errorMessage);
        const failureRedirect = redirectPath !== '/' ? redirectPath : '/seller/login';
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
