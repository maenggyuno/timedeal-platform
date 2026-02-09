import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaBars } from 'react-icons/fa';
import styles from '../../styles/seller/Header.module.css';
import { useAuth } from '../../contexts/AuthContext';
import axiosConfig from '../../services/axiosConfig'; // axios 인스턴스 파일 경로에 맞게 수정해주세요.

const Header = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // localStorage를 사용하는 useState와 useEffect를 모두 제거하고,
    // useAuth 훅을 통해 전역 상태를 가져옴
    const { isLoggedIn, setUser } = useAuth();

    const handleLoginClick = () => {
        // 로그인 후 돌아올 경로를 저장하는 로직은 그대로 유지할 수 있음
        localStorage.setItem('redirectPathAfterLogin', location.pathname);
        navigate('/login');
    };

    // 로그아웃 함수
    const handleLogoutClick = async () => {
        try {
            // 백엔드에 로그아웃 요청을 보내 서버 측 쿠키 삭제를 시도
            await axiosConfig.post('/api/auth/logout');
        } catch (error) {
            console.error('Logout API call failed, but proceeding with client-side logout:', error);
        } finally {
            setUser(null);
            navigate('/seller/login');
        }
    };

    return (
        <header className={styles.header}>
            <nav className={styles.navLinks}>
                <Link to="/seller/dashboard">동네콕 판매자센터</Link>
            </nav>
            {isLoggedIn ? (
                <div className={styles.rightSection}>
                    <button className={styles.authButton} onClick={handleLogoutClick}>
                        로그아웃
                    </button>
                </div>

            ) : (
                <div className={styles.rightSection}>

                </div>
            )}

        </header>
    );
};

export default Header;
