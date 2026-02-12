import React, {useEffect, useRef} from 'react';
import {useLocation} from 'react-router-dom';
import axiosInstance from '../../services/axiosConfig';

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

    const paymentInfoRaw = localStorage.getItem(sessionKey);

    if (!paymentInfoRaw) {
      localStorage.setItem(sessionKey, JSON.stringify({status: 'fail', message: '결제 정보를 찾을 수 없습니다.'}));
      window.close();
      return;
    }

    localStorage.removeItem(sessionKey);
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

    // 🟢 변경 후
    axiosInstance.post(fetchUrl, body)
      .then(response => {
        // Axios는 .data에 결과가 바로 들어있습니다.
        const data = response.data;

        const result = {
          status: 'success',
          finalOrderIds: isCartPayment ? data.orderIds : [data.orderId]
        };
        localStorage.setItem(sessionKey, JSON.stringify(result));
        window.close();
      })
      .catch(error => {
        // 에러 처리도 Axios 방식에 맞게 수정
        console.error("Payment Confirm Error:", error);
        const errorMsg = error.response?.data?.message || error.message || '결제 승인에 실패했습니다.';

        const result = {status: 'fail', message: errorMsg};
        localStorage.setItem(sessionKey, JSON.stringify(result));
        window.close();
      });

  }, [location]);

  return (
    <div style={{padding: '20px', textAlign: 'center'}}>
      <h1>결제를 승인하고 있습니다...</h1>
      <p>잠시만 기다려주세요. 이 창은 자동으로 닫힙니다.</p>
    </div>
  );
};

export default TossSuccess;
