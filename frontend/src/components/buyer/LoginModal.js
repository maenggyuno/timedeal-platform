import React from 'react';
import modalStyles from '../../styles/buyer/LoginModal.module.css';

const LoginModal = ({ onClose }) => {
  // const GOOGLE_AUTH_URL = 'http://localhost:8080/oauth2/authorization/google';
  // const NAVER_AUTH_URL = 'http://localhost:8080/oauth2/authorization/naver';
  const GOOGLE_AUTH_URL = 'api/oauth2/authorization/google';
  const NAVER_AUTH_URL = 'api/oauth2/authorization/naver';

  const handleSocialLogin = (platform) => {
    if (platform === 'google') {
      window.location.href = GOOGLE_AUTH_URL;
    } else if (platform === 'naver') {
      window.location.href = NAVER_AUTH_URL;
    }
  };

  return (
    <div className={modalStyles.modalOverlay} onClick={onClose}>
      <div className={modalStyles.modalContent} onClick={(e) => e.stopPropagation()}>
        <button className={modalStyles.closeButton} onClick={onClose}>&times;</button>

        <h1 className={modalStyles.title}>로그인</h1>
        <div className={modalStyles.buttonContainer}>
          <hr className={modalStyles["custom-hr"]} />
          <button
            className={`${modalStyles.loginButton} ${modalStyles.googleButton}`}
            onClick={() => handleSocialLogin('google')}
          >
            <div className={modalStyles.buttonContent}>
              <div className={modalStyles.googleIcon}></div>
              <span className={modalStyles.buttonText}>구글 아이디로 로그인</span>
            </div>
          </button>
          <button
            className={`${modalStyles.loginButton} ${modalStyles.naverButton}`}
            onClick={() => handleSocialLogin('naver')}
          >
            <div className={modalStyles.buttonContent}>
              <div className={modalStyles.naverIcon}></div>
              <span className={modalStyles.buttonText}>네이버 아이디로 로그인</span>
            </div>
          </button>
        </div>
        <div className={modalStyles.copyright}>
          © 2025 동네콕. All Rights Reserved.<br />
        </div>
      </div>
    </div>
  );
};

export default LoginModal;
