import React, { createContext, useState, useEffect, useContext } from 'react';

import api from "../services/axiosConfig";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    // 1. 권한이 있는 마트 목록을 저장할 상태 추가
    const [authorizedMarts, setAuthorizedMarts] = useState([]);

    useEffect(() => {
        const checkLoginStatus = async () => {
            try {
                const response = await api.get('/api/auth/login');
                if (response.status === 200 && response.data) {
                    // response.data 객체 전체를 user 상태에 저장
                    setUser(response.data);
                    // 2. API 응답에서 authorizedMarts 배열을 가져와 상태에 저장
                    setAuthorizedMarts(response.data.authorizedMarts || []);
                }
            } catch (error) {
                console.log("No valid session found.");
                setUser(null);
                setAuthorizedMarts([]); // 로그아웃 시 권한 목록도 초기화
            } finally {
                setLoading(false);
            }
        };

        checkLoginStatus();
    }, []);

    // 3. value 객체에 authorizedMarts 추가
    const value = { user, setUser, isLoggedIn: !!user, loading, authorizedMarts };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};
