import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import styles from '../../styles/buyer/OrderCompletePage.module.css';

// 날짜 포맷 함수
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '알 수 없음';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric', month: 'long', day: 'numeric',
        hour: '2-digit', minute: '2-digit', hour12: true
    });
};

// 개별 주문 정보를 보여주는 카드 컴포넌트
const OrderCard = ({ order }) => (
    <div className={styles.productInfo}>
        <img src={order.productImgSrc || '/no-image.png'} alt={order.productName} className={styles.productImage} />
        <div className={styles.productDetails}>
            <h2>{order.productName}</h2>
            <p>구매수량: {order.quantity}개</p>
            <p>결제금액: {order.totalPrice.toLocaleString()}원</p>
            <p className={styles.validUntil}>수령기한: {formatDateTime(order.validUntil)} 까지</p>
        </div>
    </div>
);


const OrderCompletePage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [orders, setOrders] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const orderId = queryParams.get('orderId');
        const orderIds = queryParams.get('orderIds');

        let fetchUrl = '';

        if (orderIds) {
            fetchUrl = `/api/buyer/order/cart-complete?orderIds=${orderIds}`;
        } else if (orderId) {
            fetchUrl = `/api/buyer/order/single-complete?orderId=${orderId}`;
        } else {
            setError('주문 정보가 없습니다.');
            return;
        }

        fetch(fetchUrl, { credentials: 'include' })
            .then(res => {
                if (!res.ok) throw new Error('주문 정보를 불러오는 중 오류가 발생했습니다.');
                return res.json();
            })
            .then(data => {
                if (Array.isArray(data)) {
                    setOrders(data);
                } else {
                    setOrders([data]); 
                }
            })
            .catch(err => {
                console.error("Fetch Error:", err);
                setError(err.message);
            });
            
    }, [location]);

    if (error) {
        return (
            <>
                <Header />
                <div className={styles.container}><div className={styles.card}><p>오류: {error}</p></div></div>
                <Footer />
            </>
        );
    }

    if (orders.length === 0) {
        return (
            <>
                <Header />
                <div className={styles.container}><div className={styles.card}><p>주문 정보를 불러오는 중...</p></div></div>
                <Footer />
            </>
        );
    }

    return (
        <>
            <Header />
            <main className={styles.container}>
                <div className={styles.card}>
                    <h1 className={styles.title}>주문이 완료되었습니다!</h1>
                    <p className={styles.subtitle}>매장에서 QR코드를 보여주고 상품을 수령하세요.</p>
                    
                    {orders.map(order => (
                        <OrderCard key={order.orderId} order={order} />
                    ))}

                    <button className={styles.qrButton} onClick={() => navigate('/buyer/purchase')}>
                        QR코드 확인하기
                    </button>
                    <button className={styles.homeButton} onClick={() => navigate('/buyer')}>
                        홈으로 돌아가기
                    </button>
                </div>
            </main>
            <Footer />
        </>
    );
};

export default OrderCompletePage;