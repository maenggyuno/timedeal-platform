import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchDashboardMarts } from '../../services/sellerApi';
import styles from '../../styles/seller/MainDashboard.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';

const BASE_URL = process.env.REACT_APP_API_URL || '';

const MainDashboard = () => {
  const [marts, setMarts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const navigate = useNavigate();
  const [openMenuId, setOpenMenuId] = useState(null);
  const menuRef = useRef(null);

  useEffect(() => {
    const fetchStores = async () => {
      try {
        // const response = await axios.get('/api/seller/store/dashboard');
        const response = await fetchDashboardMarts(); // 👈 [수정] 이렇게 변경!
        setMarts(response.data);
      } catch (e) {
        console.error("매장 목록을 불러오는 데 실패했습니다.", e);
        setError("데이터를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchStores();
  }, []);

  const toggleMenu = (storeId, event) => {
    event.stopPropagation();
    setOpenMenuId(openMenuId === storeId ? null : storeId);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpenMenuId(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleManage = (storeId, event) => {
    event.stopPropagation();
    navigate(`/seller/store/manage/${storeId}`);
  };

  // 매장 삭제
  const handleDelete = async (storeId, event) => {
    event.stopPropagation();
    setOpenMenuId(null);

    if (window.confirm(`매장을 정말 삭제하시겠습니까?`)) {
      try {
        // 1. 서버에 DELETE 요청 보내기
        await axios.delete(`${BASE_URL}/api/seller/store/${storeId}`);

        // 2. 요청 성공 시, 화면(state)에서 해당 매장 제거
        setMarts(currentMarts =>
          currentMarts.filter(mart => mart.storeId !== storeId)
        );

        alert('매장이 성공적으로 삭제되었습니다.');
      } catch (err) {
        // 3. 에러 처리
        console.error("매장 삭제 실패:", err);
        alert('매장 삭제 중 오류가 발생했습니다.');
      }
    }
  };

  const renderContent = () => {
    if (isLoading) return <p></p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;
    if (marts.length === 0) return <p>관리 중인 매장이 없습니다. 첫 매장을 등록해보세요!</p>;

    return marts.map(mart => (
      <div key={mart.storeId} className={styles["mart-item"]} onClick={() => navigate(`/seller/mart/${mart.storeId}`)}>
        <div className={styles["mart-info"]}>
          <h3>{mart.name}</h3>
          <p>{mart.address}</p>
        </div>
        {mart.authority === 1 && (
          <div className={styles["menu-container"]} ref={openMenuId === mart.storeId ? menuRef : null}>
            <button onClick={(e) => toggleMenu(mart.storeId, e)} className={styles["menu-button"]}>
              &#8942;
            </button>
            {openMenuId === mart.storeId && (
              <div className={styles["dropdown-menu"]}>
                <button onClick={(e) => handleManage(mart.storeId, e)}>매장 관리</button>
                <button onClick={(e) => handleDelete(mart.storeId, e)}>매장 삭제</button>
              </div>
            )}
          </div>
        )}
      </div>
    ));
  };

  return (
    <div className={styles["page-container"]}>
      <Header />
      <div className={styles["dashboard-container"]}>
        <div className={styles["dashboard-header"]}>
          <h2>내 매장 목록</h2>
          <button
            className={styles["add-store-button"]}
            onClick={() => navigate('/seller/store/create')}
          >
            + 매장 추가
          </button>
        </div>
        <div className={styles["mart-list"]}>
          {renderContent()}
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default MainDashboard;
