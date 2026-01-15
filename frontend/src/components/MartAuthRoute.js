import React from 'react';
import { Navigate, Outlet, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// 로그인 여부 + 특정 마트 접근 권한을 모두 체크
const MartAuthRoute = () => {
    const { isLoggedIn, authorizedMarts, loading } = useAuth();
    const { martId } = useParams();

    if (loading) {
        return null;
    }

    // 로그인이 안 되어있으면 무조건 로그인 페이지로
    if (!isLoggedIn) {
        return <Navigate to="/seller/login" replace />;
    }

    // authorizedMarts 배열에 현재 URL의 martId가 포함되어 있는지 확인
    const isAuthorized = authorizedMarts.includes(Number(martId));

    // 권한이 없으면 대시보드로 리다이렉트
    return isAuthorized ? <Outlet /> : <Navigate to="/seller/dashboard" replace />;
};

export default MartAuthRoute;