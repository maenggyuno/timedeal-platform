import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// 로그인 여부만 체크
const SellerPrivateRoute = () => {
    const { isLoggedIn, loading } = useAuth();

    if (loading) {
        return null;
    }

    // 로딩이 끝난 후, 로그인 상태가 아니면 로그인 페이지로 리다이렉트
    return isLoggedIn ? <Outlet /> : <Navigate to="/seller/login" replace />;
};

export default SellerPrivateRoute;