import React, { useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';

const TossSuccess = () => {
    const location = useLocation();
    const hasFetched = useRef(false);

    useEffect(() => {
        if (hasFetched.current) {
            return;
        }
        hasFetched.current = true;

        const queryParams = new URLSearchParams(location.search);
        const paymentKeyFromToss = queryParams.get('paymentKey');
        const orderIdFromToss = queryParams.get('orderId');
        const amount = queryParams.get('amount');
        
        const sessionKey = queryParams.get('sessionKey'); 
        
        const paymentInfoRaw = sessionStorage.getItem(sessionKey);

        if (!paymentInfoRaw) {
            localStorage.setItem(sessionKey, JSON.stringify({ status: 'fail', message: '결제 정보를 찾을 수 없습니다.' }));
            window.close();
            return;
        }
        
        sessionStorage.removeItem(sessionKey);
        const paymentInfo = JSON.parse(paymentInfoRaw);
        
        const isCartPayment = !!paymentInfo.items;

        let fetchUrl = '';
        let body = {};

        if (isCartPayment) {
            fetchUrl = `/api/buyer/pay/cart/process`;
            body = {
                items: paymentInfo.items,
                paymentKey: paymentKeyFromToss,
                orderId: orderIdFromToss,
                amount: Number(amount),
            };
        } else {
            fetchUrl = `/api/buyer/pay/toss/confirm`;
            body = {
                paymentKey: paymentKeyFromToss,
                orderId: orderIdFromToss,
                amount: Number(amount),
                productId: paymentInfo.productId,
                quantity: paymentInfo.quantity,
                isReservation: paymentInfo.isReservation
            };
        }

        fetch(fetchUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(body)
        })
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw new Error(err.message || '결제 승인에 실패했습니다.') });
            }
            return res.json();
        })
        .then(data => {
            const result = { 
                status: 'success', 
                finalOrderIds: isCartPayment ? data.orderIds : [data.orderId] 
            };
            localStorage.setItem(sessionKey, JSON.stringify(result));
            window.close();
        })
        .catch(error => {
            const result = { status: 'fail', message: error.message };
            localStorage.setItem(sessionKey, JSON.stringify(result));
            window.close();
        });

    }, [location]);

    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
            <h1>결제를 승인하고 있습니다...</h1>
            <p>잠시만 기다려주세요. 이 창은 자동으로 닫힙니다.</p>
        </div>
    );
};

export default TossSuccess;