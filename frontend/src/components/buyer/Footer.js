import React from 'react';
import styles from '../../styles/buyer/Footer.module.css';

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div className={styles.footerContainer}>
                {/* 왼쪽 섹션 - 사업자 정보 */}
                <div className={styles.companyInfo}>
                    <h3 className={styles.companyTitle}>사업자정보</h3>

                    <div className={styles.infoBlock}>
                        <div className={styles.infoRow}>
                            <span className={styles.highlight}>대표 : 이원희</span>
                        </div>

                        <div className={styles.infoRow}>
                            <span>사업자등록번호 : 123-45-67890</span>
                            <span>|</span>
                            <span>통신판매업신고 : 2025-서울강남-12345</span>
                        </div>

                        <div className={styles.infoRow}>
                            <span>EMAIL : a0107163@naver.com</span>
                            <span>|</span>
                            <span>FAX : 031-670-5114</span>
                        </div>

                        <div className={styles.infoRow}>
                            <span>주소 : 경기도 안성시 중앙로 327 (한경대학교)</span>
                        </div>
                    </div>
                </div>

                {/* 오른쪽 섹션 - 판매자센터 버튼과 고객센터 */}
                <div className={styles.customerService}>
                    <div className={styles.serviceTitle}>고객센터 <span>→</span></div>
                    <div className={styles.contactNumber}>031-670-5114</div>
                    <div className={styles.serviceTime}>운영시간 9시 - 18시 (주말/공휴일 휴무, 점심시간 12시 - 13시)</div>
                    <br />
                    <br />
                    <div className={styles.sellerCenterBlock}>
                        <a href="/seller/login" className={styles.sellerCenterButton}>
                            판매자 센터 이동하기
                            <span className={styles.arrowIcon}>→</span>
                        </a>
                    </div>
                </div>

                {/* 하단 면책 문구 */}
                <div className={styles.disclaimer}>
                    동네콕은 통신판매중개자이며, 통신판매의 당사자가 아닙니다. 전자상거래 등에서의 소비자보호에 관한 법률 등 관련 법령 및 동네쿡의 약관에 따라 상품, 상품정보, 거래에 관한 책임은 개별 판매자에게 귀속하고, 동네콕은 원칙적으로 회원간 거래에 대하여 책임을 지지 않습니다. 다만, 동네콕이 직접 판매하는 상품에 대한 책임은 동네콕에게 귀속합니다.
                </div>

                {/* 저작권 정보 */}
                <div className={styles.copyright}>
                    © 동네콕, Inc All rights reserved.
                </div>
            </div>
        </footer>
    );
};

export default Footer;