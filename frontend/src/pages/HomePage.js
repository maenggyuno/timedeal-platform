import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/Home.module.css';

const HomePage = () => {

  return (
    <div className={styles.container}>
      <div className={styles.loginBox}>
        <img src="/buyer/shop_logo.png" alt="쇼핑몰 이름" className={styles["shop-logo"]}/>
        
        {/* 로그인 후 화면 - 로그인 정보와 버튼들 */}
        <div className={styles.loggedInContainer}>
          <hr className={styles["custom-hr"]} />
          <div className={styles.buttonContainer}>
            <button 
              className={`${styles.loginButton} ${styles.sellerButton}`} 
              onClick={() => window.location.href = '/seller/login'}
            >
              <div className={styles.buttonContent}>
                <span className={styles.buttonText}>판매자센터 이동</span>
              </div>
            </button>
            <button 
              className={`${styles.loginButton} ${styles.buyerButton}`} 
              onClick={() => window.location.href = '/buyer'}
            >
              <div className={styles.buttonContent}>
                <span className={styles.buttonText}>구매자센터 이동</span>
              </div>
            </button>
          </div>
        </div>
        <div className={styles.copyright}>
          © 2025 동네콕. All Rights Reserved.<br />
        </div>
      </div>
    </div>
  );
};

export default HomePage;