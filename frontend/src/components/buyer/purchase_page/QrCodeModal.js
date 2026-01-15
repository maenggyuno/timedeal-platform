import React from 'react';
import { QRCodeSVG } from 'qrcode.react'; 
import styles from '../../../styles/buyer/QrCodeModal.module.css';

const QrCodeModal = ({ isOpen, onClose, qrData }) => {
  if (!isOpen || !qrData) {
    return null;
  }

  const handleContentClick = (e) => {
    e.stopPropagation();
  };

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modalContent} onClick={handleContentClick}>
        <button className={styles.closeButton} onClick={onClose}>&times;</button>
        <h2 className={styles.modalTitle}>QR 코드</h2>
        <div className={styles.qrCodeContainer}>
          <QRCodeSVG value={qrData.uuid} size={256} />
        </div>
        <p className={styles.modalDescription}>이 QR 코드는 남은 시간이 지나면 만료됩니다.</p>
        <p className={styles.modalDescription}>남은 시간이 지나기 전에 사용바랍니다.</p>
      </div>
    </div>
  );
};

export default QrCodeModal;
