import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import ModalStyles from '../../styles/buyer/PaymentModal.module.css';
import axiosConfig from '../../services/axiosConfig';

const PaymentModal = ({isOpen, onClose, product, quantity}) => {
  const navigate = useNavigate();
  const [view, setView] = useState('selection');

  useEffect(() => {
    if (isOpen) {
      setView('selection');
    }
  }, [isOpen]);

  if (!isOpen) {
    return null;
  }

  const handleOnSitePayment = () => {
    axiosConfig.post(`/api/buyer/pay/on-site?productId=${product.productId}&quantity=${quantity}`)
      .then(response => {
        // axios 성공 시 바로 데이터 처리
        alert('현장결제 예약이 완료되었습니다!');
        navigate(`/buyer/order-complete?orderId=${response.data.orderId}`);
      })
      .catch(error => {
        console.error('On-site Payment Error:', error);
        // 에러 메시지 처리 (백엔드 응답 구조에 따라 다를 수 있음)
        alert('결제에 실패했습니다.');
        onClose();
      });
  };

  const handleCardPayment = (isReservation) => {
    const sessionKey = `ss_single_${new Date().getTime()}`;
    const orderId = `single_${product.productId}_${new Date().getTime()}`;
    const priceMultiplier = isReservation ? 1.10 : 1.0;
    const finalAmount = Math.round(product.price * quantity * priceMultiplier);
    const orderName = isReservation ? `${product.name} (예약)` : product.name;

    const paymentInfo = {
      productId: product.productId,
      quantity: quantity,
      isReservation: isReservation,
      amount: finalAmount,
      sessionKey: sessionKey,
      orderId: orderId,
    };
    sessionStorage.setItem(sessionKey, JSON.stringify(paymentInfo));

    const params = new URLSearchParams({
      amount: finalAmount,
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
            const finalOrderId = result.finalOrderIds[0];
            navigate(`/buyer/order-complete?orderId=${finalOrderId}`);
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
  };

  return (
    <div className={ModalStyles.modalOverlay}>
      <div className={ModalStyles.modalContent}>
        <button className={ModalStyles.closeIcon} onClick={onClose}>&times;</button>
        {view === 'selection' && (
          <>
            <h2>결제 수단 선택</h2>
            <br/>
            <div className={ModalStyles.buttonGroup}>
              <button onClick={handleOnSitePayment}>현장결제</button>
              {product.store.paymentMethod === 1 && (
                <button onClick={() => setView('card_choice')}>카드결제</button>
              )}
            </div>
          </>
        )}
        {view === 'card_choice' && (
          <>
            <h2>카드결제 방식 선택</h2>
            <p className={ModalStyles.description}>
              예약 결제 시, 매장 마감 시간 1시간 전까지 수령이 가능합니다.
            </p>
            <br/>
            <div className={ModalStyles.buttonGroup}>
              <button onClick={() => handleCardPayment(false)}>카드결제 (기본)</button>
              <button onClick={() => handleCardPayment(true)}>카드결제 (예약)</button>
            </div>
            <button className={ModalStyles.backButton} onClick={() => setView('selection')}>
              뒤로 가기
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default PaymentModal;
