import React from 'react';
import styles from '../../styles/seller/LoginPage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';

const LoginPage = () => {
  // 백엔드(Spring)의 소셜 로그인 URL을 정의
  // const GOOGLE_LOGIN_URL = 'http://localhost:8080/oauth2/authorization/google';
  // const NAVER_LOGIN_URL = 'http://localhost:8080/oauth2/authorization/naver';
  // const GOOGLE_LOGIN_URL = '/oauth2/authorization/google';
  // const NAVER_LOGIN_URL = '/oauth2/authorization/naver';

  // 환경 변수에서 베이스 URL을 가져옵니다.
  // 없으면 기본값으로 상대 경로를 사용하도록 설정합니다.
  const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || '';

  const GOOGLE_LOGIN_URL = `${BACKEND_URL}/oauth2/authorization/google`;
  const NAVER_LOGIN_URL = `${BACKEND_URL}/oauth2/authorization/naver`;

  const handleSocialLogin = (provider) => {
    localStorage.setItem('redirectPathAfterLogin', '/seller/dashboard');

    const url = provider === 'google' ? GOOGLE_LOGIN_URL : NAVER_LOGIN_URL;
    window.location.href = url;
  };

  return (
    <div className={styles.pageContainer}>
      <Header />
      <main className={styles.mainContent}>
        <div className={styles.imageSection}>
          <img src="/seller-login.png" alt="로그인 페이지 홍보 이미지" className={styles.promoImage} />
        </div>
        <div className={styles.loginSection}>
          <h2>로그인</h2>
          <p>소셜 계정으로 간편하게 로그인하세요.</p>
          <div className={styles.socialLoginButtons}>
            <button
              className={`${styles.socialButton} ${styles.googleButton}`}
              onClick={() => handleSocialLogin('google')}
            >
              <span>Google 계정으로 로그인</span>
            </button>
            <button
              className={`${styles.socialButton} ${styles.naverButton}`}
              onClick={() => handleSocialLogin('naver')}
            >
              <span>네이버 계정으로 로그인</span>
            </button>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default LoginPage;
