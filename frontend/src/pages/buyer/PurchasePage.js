import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import PurchaseStatus from '../../components/buyer/purchase_page/PurchaseStatus';
import StoreLocationModal from '../../components/buyer/product_info_store_location/StoreLocationModal';
import ReviewModal from '../../components/buyer/ReviewModal';
import QrCodeModal from '../../components/buyer/purchase_page/QrCodeModal';
import styles from '../../styles/buyer/PurchasePage.module.css';

const PurchasePage = () => {
  const [activeTab, setActiveTab] = useState('purchasing');
  const [purchasingItems, setPurchasingItems] = useState([]);
  const [completedItems, setCompletedItems] = useState([]);
  const [expiredItems, setExpiredItems] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isStoreModalOpen, setIsStoreModalOpen] = useState(false);
  const [selectedStore, setSelectedStore] = useState(null);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [reviewModalMode, setReviewModalMode] = useState('create');
  const [selectedReviewData, setSelectedReviewData] = useState(null);
  const [isQrModalOpen, setIsQrModalOpen] = useState(false);
  const [selectedQrData, setSelectedQrData] = useState(null);

  const fetchData = async (endpoint, setItems) => {
    try {
      setIsLoading(true);
      setError(null);
      setItems([]);
      const response = await axios.get(endpoint, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      setItems(response.data);
    } catch (err) {
      setError('데이터를 불러오는 데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const reloadCompletedItems = () => {
    fetchData('/api/buyer/order/buy-completed', setCompletedItems);
  };

  const reloadPurchasingItems = () => {
    fetchData('/api/buyer/order/buy-ing', setPurchasingItems);
  };

  useEffect(() => {
    if (activeTab === 'purchasing') reloadPurchasingItems();
    else if (activeTab === 'expired') fetchData('/api/buyer/order/buy-cancel', setExpiredItems);
    else if (activeTab === 'completed') reloadCompletedItems();
  }, [activeTab]);

  const handleTabChange = (tab) => setActiveTab(tab);

  const handleOpenStoreModal = (item) => {
    if (item.storeIsDeleted) {
      alert('현재 상점은 폐쇄되었습니다.');
      return; 
    }
    setSelectedStore({ storeId: item.storeId, name: item.storeName || item.name, address: item.storeAddress || item.address });
    setIsStoreModalOpen(true);
  };
  
  const handleCloseStoreModal = () => setIsStoreModalOpen(false);

  const handleWriteReview = (orderId) => {
    setReviewModalMode('create');
    setSelectedReviewData({ orderId });
    setIsReviewModalOpen(true);
  };

  const handleCheckReview = async (orderId) => {
    try {
      const response = await axios.get(`/api/buyer/review/${orderId}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      setReviewModalMode('view');
      setSelectedReviewData({ orderId, ...response.data });
      setIsReviewModalOpen(true);
    } catch (err) {
      console.error("리뷰 조회 실패:", err);
      alert("리뷰를 불러오는 데 실패했습니다.");
    }
  };

  const handleCloseReviewModal = (isSubmitted) => {
    setIsReviewModalOpen(false);
    setSelectedReviewData(null);
    if (isSubmitted) {
      reloadCompletedItems();
    }
  };

  const handleExtendTime = async (orderId) => {
    if (!window.confirm('정말로 시간을 연장하시겠습니까?')) {
      return;
    }
    try {
      const response = await axios.post(`/api/buyer/order/valid-until-extension?orderId=${orderId}`, null, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      alert(response.data);
      reloadPurchasingItems();
    } catch (error) {
      console.error('시간 연장 요청 실패:', error);
      alert(error.response?.data || '시간 연장에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleCheckQrCode = async (orderId) => {
    try {
      const response = await axios.get(`/api/qrCode/get?orderId=${orderId}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      setSelectedQrData(response.data);
      setIsQrModalOpen(true);
    } catch (err) {
      console.error("QR 코드 조회 실패:", err);
      alert("QR 코드를 불러오는 데 실패했습니다.");
    }
  };
  
  const handleCloseQrModal = () => setIsQrModalOpen(false);

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('정말로 구매를 취소하시겠습니까?')) {
      return;
    }
    try {
      const response = await axios.post(`/api/buyer/order/cancel?orderId=${orderId}`, null, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      alert(response.data);
      reloadPurchasingItems();
    } catch (error) {
      console.error('구매 취소 요청 실패:', error);
      alert(error.response?.data || '구매 취소에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <div className={styles.container}>
      <Header />
      <main className={styles.main}>
        <PurchaseStatus 
          activeTab={activeTab} 
          onTabChange={handleTabChange} 
          purchasingItems={purchasingItems} 
          completedItems={completedItems}
          expiredItems={expiredItems}
          onStoreClick={handleOpenStoreModal}
          onWriteReview={handleWriteReview}
          onCheckReview={handleCheckReview}
          onExtendTime={handleExtendTime}
          onCheckQrCode={handleCheckQrCode}
          onCancelOrder={handleCancelOrder}
          isLoading={isLoading}
          error={error}
        />
      </main>
      
      {isStoreModalOpen && (
        <StoreLocationModal 
          isOpen={isStoreModalOpen}
          onClose={handleCloseStoreModal}
          storeId={selectedStore.storeId}
          storeName={selectedStore.name}
          storeAddress={selectedStore.address}
        />
      )}

      {isReviewModalOpen && (
        <ReviewModal
          isOpen={isReviewModalOpen}
          onClose={handleCloseReviewModal}
          mode={reviewModalMode}
          orderId={selectedReviewData?.orderId}
          reviewData={selectedReviewData}
        />
      )}

      {isQrModalOpen && (
        <QrCodeModal
          isOpen={isQrModalOpen}
          onClose={handleCloseQrModal}
          qrData={selectedQrData}
        />
      )}

      <Footer />
    </div>
  );
};

export default PurchasePage;