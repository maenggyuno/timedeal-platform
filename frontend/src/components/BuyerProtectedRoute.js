import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * 로그인이 필요한 페이지를 감싸는 컴포넌트
 * 로그인하지 않은 사용자는 지정된 경로로 리다이렉트
 */
const BuyerProtectedRoute = ({ children }) => {

    // AuthProvider로부터 로그인 상태를 가져옴
    const { isLoggedIn } = useAuth();
    const location = useLocation();

    if (!isLoggedIn) {
        // 로그인하지 않았다면, /buyer 페이지로 리다이렉트
        alert('허용되지 않은 접근입니다.');
        return <Navigate to="/buyer" state={{ from: location }} replace />;
    }

    return children;
};

export default BuyerProtectedRoute;