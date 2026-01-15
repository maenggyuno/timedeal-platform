import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../../styles/buyer/CartPaymentModal.module.css';

const CartPaymentModal = ({ isOpen, onClose, items }) => {
    const navigate = useNavigate();
    const [paymentOptions, setPaymentOptions] = useState({});
    const [totalAmount, setTotalAmount] = useState(0);

    useEffect(() => {
        if (items.length > 0) {
            const initialOptions = {};
            items.forEach(item => {
                initialOptions[item.productId] = {
                    paymentMethod: item.storePaymentMethod === 0 ? 'ON_SITE' : 'CARD',
                    isReservation: false
                };
            });
            setPaymentOptions(initialOptions);
        }
    }, [items]);

    useEffect(() => {
        const newTotalAmount = items.reduce((sum, item) => {
            const option = paymentOptions[item.productId];
            let itemPrice = item.price * item.cartQuantity;
            if (option && option.paymentMethod === 'CARD' && option.isReservation) {
                itemPrice *= 1.10;
            }
            return sum + itemPrice;
        }, 0);
        setTotalAmount(Math.round(newTotalAmount));
    }, [items, paymentOptions]);

    const handleOptionChange = (productId, selectedValue) => {
        let paymentMethod = 'ON_SITE';
        let isReservation = false;

        if (selectedValue === 'CARD_BASIC') {
            paymentMethod = 'CARD';
            isReservation = false;
        } else if (selectedValue === 'CARD_RESERVED') {
            paymentMethod = 'CARD';
            isReservation = true;
        }

        setPaymentOptions(prev => ({
            ...prev,
            [productId]: { paymentMethod, isReservation }
        }));
    };

    const getSelectedValue = (productId) => {
        const option = paymentOptions[productId];
        if (!option) return 'ON_SITE';
        if (option.paymentMethod === 'CARD') {
            return option.isReservation ? 'CARD_RESERVED' : 'CARD_BASIC';
        }
        return 'ON_SITE';
    };

    const handlePayment = () => {
        const cardItems = items.filter(item => paymentOptions[item.productId]?.paymentMethod === 'CARD');
        const finalItemsPayload = items.map(item => ({
            productId: item.productId,
            quantity: item.cartQuantity,
            ...paymentOptions[item.productId]
        }));

        if (cardItems.length > 0) {
            const totalCardAmount = cardItems.reduce((sum, item) => {
                let itemPrice = item.price * item.cartQuantity;
                if (paymentOptions[item.productId]?.isReservation) {
                    itemPrice *= 1.10;
                }
                return sum + itemPrice;
            }, 0);

            const sessionKey = `ss_cart_${new Date().getTime()}`;
            const orderId = 'cart_' + new Date().getTime().toString() + Math.random().toString(36).substr(2, 9);
            const orderName = `${items[0].productName} 외 ${items.length - 1}건`;

            const paymentInfo = {
                items: finalItemsPayload,
                amount: Math.round(totalCardAmount),
                sessionKey: sessionKey,
                orderId: orderId,
            };
            sessionStorage.setItem(sessionKey, JSON.stringify(paymentInfo));
            
            const params = new URLSearchParams({
                amount: Math.round(totalCardAmount),
                orderName: orderName,
                sessionKey: sessionKey,
                orderId: orderId,
            }).toString();

            const popup = window.open(`/toss-checkout?${params}`, 'toss-payment', 'width=800,height=600');
            
            const interval = setInterval(() => {
                if (popup && popup.closed) {
                    clearInterval(interval);
                    const paymentResult = localStorage.getItem(sessionKey);
                    if (paymentResult) {
                        localStorage.removeItem(sessionKey);
                        const result = JSON.parse(paymentResult);

                        if (result.status === 'success') {
                            alert('결제가 성공적으로 완료되었습니다.');
                            const orderIdsString = result.finalOrderIds.join(',');
                            navigate(`/buyer/order-complete?orderIds=${orderIdsString}`);
                        } else {
                            alert(`결제에 실패했습니다: ${result.message}`);
                            onClose();
                        }
                    } else {
                        console.log("결제가 사용자에 의해 취소되었습니다.");
                        onClose();
                    }
                }
            }, 500);

        } else {
            sendOnSitePaymentRequest(finalItemsPayload);
        }
    };

    const sendOnSitePaymentRequest = async (itemsPayload) => {
        try {
            const response = await fetch('/api/buyer/pay/cart/process', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ items: itemsPayload })
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '결제 처리 중 오류가 발생했습니다.');
            }
            const result = await response.json();
            alert('현장결제 예약이 성공적으로 완료되었습니다.');
            const orderIdsString = result.orderIds.join(',');
            navigate(`/buyer/order-complete?orderIds=${orderIdsString}`);
        } catch (error) {
            console.error('백엔드 결제 요청 실패:', error);
            alert(error.message);
        }
    };

    if (!isOpen) return null;

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modalContent}>
                <button className={styles.closeIcon} onClick={onClose}>&times;</button>
                <h2>결제하기</h2>
                <div className={styles.itemList}>
                    {items.map(item => (
                        <div key={item.productId} className={styles.item}>
                            <span className={styles.itemName}>{item.productName} ({item.cartQuantity}개)</span>
                            <select
                                className={styles.paymentSelect}
                                value={getSelectedValue(item.productId)}
                                onChange={(e) => handleOptionChange(item.productId, e.target.value)}
                            >
                                <option value="ON_SITE">현장결제</option>
                                {item.storePaymentMethod === 1 && (
                                    <>
                                        <option value="CARD_BASIC">카드결제</option>
                                        <option value="CARD_RESERVED">카드결제 (예약)</option>
                                    </>
                                )}
                            </select>
                        </div>
                    ))}
                </div>
                <div className={styles.summary}>
                    <p className={styles.totalAmount}>총 결제금액: {totalAmount.toLocaleString()}원</p>
                    <div className={styles.buttons}>
                        <button onClick={onClose} className={styles.closeButton}>닫기</button>
                        <button onClick={handlePayment} className={styles.paymentButton}>결제하기</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPaymentModal;