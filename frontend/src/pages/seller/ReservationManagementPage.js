import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from '../../styles/seller/ReservationManagementPage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';
import Sidebar from '../../components/seller/Sidebar';

// const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const ReservationManagementPage = () => {
    const { martId } = useParams();
    const navigate = useNavigate();

    const [tab, setTab] = useState('checkin');
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState(null);

    const [searchType, setSearchType] = useState('buyerName');
    const [searchQuery, setSearchQuery] = useState('');

    const isCheckedIn = (status) => status === 4;

    const humanizeRemaining = (iso) => {
        if (!iso) return '-';

        // YYYY-MM-DD HH:MM:SS -> YYYY-MM-DDTHH:MM:SS
        let correctedIso = iso.replace(' ', 'T');

        if (!correctedIso.endsWith('Z')) {
            correctedIso += 'Z';
        }
        
        const end = new Date(correctedIso);
        const now = new Date();
        const diff = Math.max(0, end - now);

        // 밀리초를 시, 분, 초로 변환
        const h = Math.floor(diff / (1000 * 60 * 60));
        const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const s = Math.floor((diff % (1000 * 60)) / 1000);

        // '시:분:초' 형식으로 반환
        return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    };

    useEffect(() => {
        if (tab !== 'checkin') return;
        const t = setInterval(() => {
            setRows((r) => [...r]);
        }, 1000);
        return () => clearInterval(t);
    }, [tab]);

    const fetchList = async () => {
        setLoading(true);
        setErr(null);
        try {
            // const res = await fetch(`${API_BASE}/api/seller/${martId}/orders?tab=${tab}`, {
            const res = await fetch(`/api/seller/${martId}/orders?tab=${tab}`, {
                credentials: 'include',
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            setRows(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
            setErr('목록을 불러오지 못했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!martId) {
            setLoading(false);
            return;
        }
        fetchList();
    }, [martId, tab]);

    const handleCancel = async (orderId) => {
        if (!window.confirm('이 예약을 취소하고 재고를 복구할까요?')) return;
        try {
            // const res = await fetch(`${API_BASE}/api/seller/orders/${orderId}/cancel`, {
            const res = await fetch(`/api/seller/orders/${orderId}/cancel`, {
                method: 'PATCH',
                credentials: 'include',
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            fetchList();
        } catch (e) {
            alert('판매 취소에 실패했습니다.');
            console.error(e);
        }
    };

    const getPaymentStatus = (status) => {
        if (status === 2 || status === 3) {
            return <span className={styles.green}>결제 완료</span>;
        }
        if (status === 1) {
            return <span className={styles.orange}>결제필요</span>;
        }
        return '-';
    };

    const filteredRows = rows.filter(row => {
        if (searchQuery.trim() === '') {
            return true;
        }
        const targetValue = searchType === 'buyerName' 
            ? row.buyerName 
            : row.productName;

        return targetValue.toLowerCase().includes(searchQuery.toLowerCase().trim());
    });

    if (!martId) {
        return <p>오류: martId가 URL에 없습니다. URL을 확인해주세요.</p>;
    }

    return (
        <>
            <Header/>
            <Sidebar base={`/seller/mart/${martId}`}/>

            <div className={styles.page}>
                <main className={styles.main}>
                    <h2 className={styles.title}>주문 목록 현황</h2>

                    <div className={styles.controlsContainer}>
                        <div className={styles.tabGroup}>
                            <button
                                className={`${styles.tab} ${tab === 'checkin' ? styles.activeTab : ''}`}
                                onClick={() => setTab('checkin')}
                            >
                                체크인 대기
                            </button>
                            <button
                                className={`${styles.tab} ${tab === 'completed' ? styles.activeTab : ''}`}
                                onClick={() => setTab('completed')}
                            >
                                판매완료
                            </button>
                        </div>
                    </div>
                    
                    <div className={styles.searchContainer}>
                        <select 
                            className={styles.searchSelect}
                            value={searchType}
                            onChange={(e) => setSearchType(e.target.value)}
                        >
                            <option value="buyerName">주문자명</option>
                            <option value="productName">주문상품</option>
                        </select>
                        <input
                            type="text"
                            className={styles.searchInput}
                            placeholder="검색어를 입력하세요"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    {loading && <p>불러오는 중…</p>}
                    {err && <p className={styles.error}>{err}</p>}

                    {!loading && !err && (
                        <div className={styles.cardGrid}>
                            {filteredRows.length > 0 ? (
                                filteredRows.map((r) => (
                                    <div key={r.orderId} className={styles.card}>
                                        <div className={styles.cardRow}>
                                            <span className={styles.cardLabel}>주문자명</span>
                                            <span className={styles.cardValue}>{r.buyerName}</span>
                                        </div>
                                        <div className={styles.cardRow}>
                                            <span className={styles.cardLabel}>주문상품</span>
                                            <span className={`${styles.cardValue} ${styles.productName}`}>{r.productName}</span>
                                        </div>
                                        <div className={styles.cardRow}>
                                            <span className={styles.cardLabel}>수량</span>
                                            <span className={styles.cardValue}>{r.quantity}</span>
                                        </div>
                                        <div className={styles.cardRow}>
                                            <span className={styles.cardLabel}>총 결제금액</span>
                                            <span className={styles.cardValue}>{(r.totalPrice ?? 0).toLocaleString()}원</span>
                                        </div>
                                        {tab === 'checkin' ? (
                                            <div className={styles.cardRow}>
                                                <span className={styles.cardLabel}>주문만료시간</span>
                                                <span className={styles.cardValue}>{humanizeRemaining(r.qrValidUntil)}</span>
                                            </div>
                                        ) : (
                                            <div className={styles.cardRow}>
                                                <span className={styles.cardLabel}>판매완료시간</span>
                                                <span className={styles.cardValue}>{r.soldAt ?? '-'}</span>
                                            </div>
                                        )}
                                        {tab === 'checkin' && (
                                            <div className={styles.cardRow}>
                                                <span className={styles.cardLabel}>결제수단</span>
                                                <span className={styles.cardValue}>{getPaymentStatus(r.status)}</span>
                                            </div>
                                        )}
                                        <div className={styles.cardActions}>
                                            {tab === 'checkin' ? (
                                                isCheckedIn(r.status) ? (
                                                    <span className={`${styles.cardValue} ${styles.green}`}>체크인됨</span>
                                                ) : (
                                                    <button className={styles.actionButton} onClick={() => navigate(`/seller/mart/${martId}/qr-scan?orderId=${r.orderId}`)}>
                                                        판매확정
                                                    </button>
                                                )
                                            ) : (
                                                <span className={`${styles.cardValue} ${styles.green}`}>판매완료</span>
                                            )}
                                            <button className={styles.actionButton} onClick={() => handleCancel(r.orderId)}>
                                                판매취소
                                            </button>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p>{searchQuery ? "검색 결과가 없습니다." : "내역이 없습니다."}</p>
                            )}
                        </div>
                    )}
                </main>
            </div>
            <Footer/>
        </>
    );
};

export default ReservationManagementPage;