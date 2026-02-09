import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/axiosConfig';

const AuthCallback = () => {
  const navigate = useNavigate();
  const hasProcessed = useRef(false);

  useEffect(() => {

    if (hasProcessed.current) {
      return;
    }
    hasProcessed.current = true;

    const processLogin = async () => {
      try {
          // 로그인 확인 API 호출
          // 성공 시 AuthProvider가 자동으로 상태를 업데이트
          await api.get('/api/auth/login');
      } catch (error) {
          // 에러가 발생해도 AuthProvider가 user 상태를 null로 유지
          console.error('AuthCallback: Login check failed.', error);
      } finally {
          // 처리가 끝나면 무조건 페이지를 이동
          const redirectPath = localStorage.getItem('redirectPathAfterLogin') || '/';
          localStorage.removeItem('redirectPathAfterLogin');
          navigate(redirectPath, { replace: true });
      }
    };

    processLogin();

  }, [navigate]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <p>로그인 정보를 처리 중입니다. 잠시만 기다려주세요...</p>
    </div>
  );
};

export default AuthCallback;
