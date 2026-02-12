import React from 'react';
import styles from '../../styles/seller/Footer.module.css';
import {Link} from "react-router-dom";

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div className={styles.footerContainer}>
                {/* 왼쪽 섹션 - 사업자 정보 */}
                <div className={styles.companyInfo}>
                    <h3 className={styles.footerTitle}>동네쿡 판매자 센터</h3>
                  <div className={styles.infoBlock}>
                    <div className={styles.infoRow}>
                      <span className={styles.highlight}>대표 : 맹균오</span>
                    </div>
                    <div className={styles.infoRow}>
                      <span>사업자등록번호 : 123-45-67890</span>
                      <span className={styles.separator}>|</span>
                      <span>통신판매업신고 : 2026-서울강남-12345</span>
                    </div>
                    <div className={styles.infoRow}>
                      <span>EMAIL : a0101234@naver.com</span>
                      <span className={styles.separator}>|</span>
                      <span>FAX : 031-670-5114</span>
                    </div>
                    <div className={styles.infoRow}>
                      <span>주소 : 경기도 안성시 중앙로 327 (한경대학교)</span>
                    </div>
                    <div className={styles.infoRow}>
                      <span>본 사이트는 포트폴리오 용도로 제작되었으며, 실제 상거래는 이루어지지 않습니다.</span>
                    </div>
                  </div>
                </div>

              {/* 오른쪽 섹션 - 구매자센터 버튼과 고객센터 */}
              <div className={styles.customerService}>
              <div className={styles.serviceTitle}>고객센터</div>
                    <div className={styles.contactNumber}>031-670-5114</div>
                    <div className={styles.serviceTime}>운영시간 9시 - 18시 (주말/공휴일 휴무, 점심시간 12시 - 13시)</div>
                    <div className={styles.actionBlock}>
                         <Link to="/buyer" className={styles.actionButton}>
                            구매자 센터 이동하기
                            <span className={styles.arrowIcon}>→</span>
                        </Link>
                    </div>
                </div>
            </div>

            {/* 하단 저작권 정보 */}
            <div className={styles.copyright}>
                © 2025 동네쿡 - 판매자 센터 | All rights reserved.
            </div>
        </footer>
    );
};

export default Footer;
