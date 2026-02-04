import React, { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { FaClock, FaShoppingCart, FaCheckCircle } from "react-icons/fa";
import styles from "../../styles/seller/MartDashboard.module.css";
import Header from "../../components/seller/Header";
import Footer from "../../components/seller/Footer";
import Sidebar from "../../components/seller/Sidebar";

// 페이지네이션 컴포넌트
const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;
  return (
    <div className={styles.pagination}>
      <button onClick={() => onPageChange(page - 1)} disabled={page === 0}>
        이전
      </button>
      <span>
        {page + 1} / {totalPages}
      </span>
      <button onClick={() => onPageChange(page + 1)} disabled={page + 1 >= totalPages}>
        다음
      </button>
    </div>
  );
};

// 남은 시간 포맷 함수
const formatTimeLeft = (iso, prefix) => {
  if (!iso) return "";
  const end = new Date(iso);
  if (isNaN(end.getTime())) return "";

  const now = new Date();
  const diffMs = end - now;
  if (diffMs <= 0) return `${prefix} 만료`;

  const totalMinutes = Math.floor(diffMs / 60000);
  const days = Math.floor(totalMinutes / (60 * 24));
  if (days > 0) return `${prefix}: ${days}일 후 만료`;
  const hours = Math.floor(totalMinutes / 60);
  if (hours > 0) return `${prefix}: ${hours}시간 후 만료`;
  return `${prefix}: ${totalMinutes}분 후 만료`;
};

const MartDashboard = () => {
  const { martId } = useParams();

  // 각 섹션별 상태 (데이터 목록, 페이지 정보)
  const [reservations, setReservations] = useState({ content: [], totalPages: 0 });
  const [purchases, setPurchases] = useState({ content: [], totalPages: 0 });
  const [completed, setCompleted] = useState({ content: [], totalPages: 0 });

  // 각 섹션별 현재 페이지 번호
  const [resPage, setResPage] = useState(0);
  const [purPage, setPurPage] = useState(0);
  const [comPage, setComPage] = useState(0);

  const fetchData = useCallback(async (endpoint, setter, page) => {
    try {
      const res = await fetch(`${BASE_URL}/api/seller/stores/${martId}/dashboard/${endpoint}?page=${page}`);
      if (res.ok) {
        const data = await res.json();
        setter({ content: data.content || [], totalPages: data.totalPages || 0 });
      } else {
        setter({ content: [], totalPages: 0 });
      }
    } catch (error) {
      console.error(`Failed to fetch ${endpoint}:`, error);
      setter({ content: [], totalPages: 0 });
    }
  }, [martId]);

  useEffect(() => { fetchData('reservations', setReservations, resPage); }, [martId, resPage, fetchData]);
  useEffect(() => { fetchData('purchases', setPurchases, purPage); }, [martId, purPage, fetchData]);
  useEffect(() => { fetchData('completed', setCompleted, comPage); }, [martId, comPage, fetchData]);

  return (
    <>
      <Header />
      <div className={styles.pageWrapper}>
        <Sidebar base={`/seller/mart/${martId}`} />
        <main className={styles.mainContent}>
          <div className={styles.whiteWrapper}>
            <div className={styles.sectionGrid}>

              {/* (1) 실시간 예약 현황 */}
              <div className={`${styles.section} ${styles.sectionFixed}`}>
                <h2><FaClock /> 실시간 예약 현황</h2>
                <ul>
                  {reservations.content.length > 0 ? reservations.content.map(item => (
                    <li key={`res-${item.order_id}`}>
                      <div className={styles.productName}>
                        {item.user_name || '고객'} | {item.product_name || '상품'} (수량 : {item.quantity ?? 1})
                      </div>
                      <div className={styles.timeLeft}>
                        {formatTimeLeft(item.valid_until, '예약 만료')}
                      </div>
                    </li>
                  )) : <li>예약 현황이 없습니다.</li>}
                </ul>
                <Pagination page={resPage} totalPages={reservations.totalPages} onPageChange={setResPage} />
              </div>

              {/* (2) 실시간 구매 현황 */}
              <div className={`${styles.section} ${styles.sectionFixed}`}>
                <h2><FaShoppingCart /> 실시간 구매 현황</h2>
                <ul>
                  {purchases.content.length > 0 ? purchases.content.map(item => (
                    <li key={`pur-${item.order_id}`}>
                      <div className={styles.productName}>
                        {item.user_name || '고객'} | {item.product_name || '상품'} (수량 : {item.quantity ?? 1})
                      </div>
                      <div className={styles.timeLeft}>
                        {formatTimeLeft(item.valid_until, '구매 만료')}
                      </div>
                    </li>
                  )) : <li>구매 현황이 없습니다.</li>}
                </ul>
                <Pagination page={purPage} totalPages={purchases.totalPages} onPageChange={setPurPage} />
              </div>

              {/* (3) 판매 완료 상품 */}
              <div className={`${styles.section} ${styles.sectionFixed}`}>
                <h2><FaCheckCircle /> 판매 완료 상품</h2>
                <ul>
                  {completed.content.length > 0 ? completed.content.map(item => (
                    <li key={`com-${item.order_id}`}>
                       <div className={styles.productName}>
                        {item.user_name || '고객'} | {item.product_name || '상품'}
                      </div>
                      <div className={styles.timeLeft}>
                        {item.quantity ?? 0}개 · 판매완료
                      </div>
                    </li>
                  )) : <li>판매 완료된 상품이 없습니다.</li>}
                </ul>
                <Pagination page={comPage} totalPages={completed.totalPages} onPageChange={setComPage} />
              </div>

            </div>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default MartDashboard;
