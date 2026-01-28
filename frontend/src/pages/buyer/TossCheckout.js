import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';

const TossCheckout = () => {
    const location = useLocation();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const amount = queryParams.get('amount');
        const orderName = queryParams.get('orderName');
        const sessionKey = queryParams.get('sessionKey');
        const orderId = queryParams.get('orderId');

        if (!orderId || !sessionKey) {
            alert("결제 정보가 올바르지 않습니다.");
            window.close();
            return;
        }

        const customerKey = `user_${new Date().getTime()}`;

        loadTossPayments(process.env.REACT_APP_TOSS_PAYMENTS_CLIENT_KEY)
            .then(tossPayments => {
                const payment = tossPayments.payment({ customerKey });
                payment.requestPayment({
                    method: 'CARD',
                    amount: { currency: 'KRW', value: Number(amount) },
                    orderId: orderId,
                    orderName: orderName,
                    successUrl: `${window.location.origin}/toss/success?sessionKey=${sessionKey}`,
                    failUrl: `${window.location.origin}/toss/fail?sessionKey=${sessionKey}`,
                    customerName: '김토스',
                })
                .catch(error => {
                    if (error.code === 'USER_CANCEL') {
                        window.close();
                    } else {
                        alert(`결제창 호출에 실패했습니다: ${error.message}`);
                        window.close();
                    }
                });
            });
    }, [location]);

    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
            <h1>결제를 진행합니다...</h1>
            <p>결제창이 뜨지 않으면 이 창을 닫고 다시 시도해주세요.</p>
        </div>
    );
};

export default TossCheckout;
