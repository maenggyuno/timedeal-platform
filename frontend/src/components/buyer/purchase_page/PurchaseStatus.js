import React from 'react';
import CountdownTimer from './CountdownTimer';
import styles from '../../../styles/buyer/PurchaseStatus.module.css';

const PurchaseStatus = ({ activeTab, onTabChange, purchasingItems, completedItems, expiredItems, onStoreClick, onWriteReview, onCheckReview, isLoading, error, onExtendTime, onCheckQrCode, onCancelOrder }) => {

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
  };

  const renderContent = () => {
    if (isLoading) return <div className={styles.message}>로딩 중...</div>;
    if (error) return <div className={`${styles.message} ${styles.error}`}>{error}</div>;

    let itemsToDisplay;
    let currentTabType;

    switch (activeTab) {
      case 'purchasing': itemsToDisplay = purchasingItems; currentTabType = 'purchasing'; break;
      case 'completed': itemsToDisplay = completedItems; currentTabType = 'completed'; break;
      case 'expired': itemsToDisplay = expiredItems; currentTabType = 'expired'; break;
      default: itemsToDisplay = [];
    }
    
    if (itemsToDisplay.length === 0) return <div className={styles.message}>해당하는 상품이 없습니다.</div>;

    return itemsToDisplay.map(item => (
      <div key={item.orderId || item.productId || item.id} className={styles.productCard}>
        <div className={styles.imageAndQrContainer}>
          <div className={styles.imageContainer}>
            <img src={item.productImgSrc || item.imageUrl || item.image || '/no-image.png'} alt={item.productName} className={styles.productImage} />
          </div>
          {currentTabType === 'purchasing' && (
            <div className={styles.qrButtonContainer}>
              <button
                className={styles.checkQrButton}
                onClick={() => onCheckQrCode(item.orderId)}
              >
                QR코드 확인
              </button>
            </div>
          )}
        </div>

        <div className={styles.productDetails}>
          <div className={`${styles.productInfo} ${styles.storeLink}`} onClick={() => onStoreClick(item)}>
            상점 정보 &gt;
          </div>
          <div className={styles.productName}>{item.productName}</div>
          <div className={styles.price}>구매수량 : {item.quantity}</div>
          <div className={styles.price}>{`${item.totalPrice.toLocaleString()}원`}</div>
          {currentTabType === 'purchasing' && (
            <div className={styles.price}>결제수단 : {item.creditMethod}</div>
          )}
          
          {currentTabType === 'purchasing' && (
            <>
              <div className={styles.timeContainer}>
                <div><span className={styles.remainingTimeLabel}>남은시간</span><span className={styles.remainingTime}><CountdownTimer expiryTimestamp={item.validUntil} /></span></div>
                <div className={styles.buttonGroup}>
                  <button
                    className={styles.extendTimeButton}
                    onClick={() => onExtendTime(item.orderId)}
                  >
                    시간연장
                  </button>
                  <button
                    className={styles.cancelButton}
                    onClick={() => onCancelOrder(item.orderId)}
                  >
                    구매취소
                  </button>
                </div>
              </div>
            </>
          )}

          {currentTabType === 'completed' && (
             <div className={styles.reviewContainer}>
              <div>
                <span className={styles.purchaseLabel}>
                  {item.isReviewed ? `리뷰 작성일: ${formatDate(item.reviewCreatedAt)}` : '구매 확정됨'}
                </span>
              </div>
              {item.isReviewed ? (
                <button className={styles.checkReviewButton} onClick={() => onCheckReview(item.orderId)}>리뷰 확인</button>
              ) : (
                <button className={styles.writeReviewButton} onClick={() => onWriteReview(item.orderId)}>리뷰 작성</button>
              )}
            </div>
          )}
        </div>
      </div>
    ));
  };
  
  return (
    <div className={styles.purchaseStatusContainer}>
      <div className={styles.tabs}>
        <div className={`${styles.tab} ${activeTab === 'purchasing' ? styles.active : ''}`} onClick={() => onTabChange('purchasing')}>구매 중</div>
        <div className={`${styles.tab} ${activeTab === 'expired' ? styles.active : ''}`} onClick={() => onTabChange('expired')}>구매 취소</div>
        <div className={`${styles.tab} ${activeTab === 'completed' ? styles.active : ''}`} onClick={() => onTabChange('completed')}>구매 완료</div>
      </div>
      <div className={styles.productsGrid}>{renderContent()}</div>
    </div>
  );
};

export default PurchaseStatus;