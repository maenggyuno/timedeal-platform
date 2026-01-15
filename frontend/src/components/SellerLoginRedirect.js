import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// 이미 로그인한 사용자를 위한 리다이렉트 컴포넌트
const SellerLoginRedirect = () => {
    const { isLoggedIn, loading } = useAuth();

    if (loading) {
        return null;
    }

    // 로딩 완료 후, 만약 로그인 상태라면 대시보드로 보내버림
    return isLoggedIn ? <Navigate to="/seller/dashboard" replace /> : <Outlet />;
};

export default SellerLoginRedirect;