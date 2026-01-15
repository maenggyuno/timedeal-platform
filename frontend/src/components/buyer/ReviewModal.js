import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../../styles/buyer/ReviewModal.module.css';

const ReviewModal = ({ isOpen, onClose, mode, orderId, reviewData }) => {
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    if (mode === 'view' && reviewData) {
      setRating(reviewData.rating);
      setContent(reviewData.content);
    } else {
      setRating(5);
      setContent('');
    }
  }, [mode, reviewData]);

  const handleSubmit = async () => {
    if (content.trim() === '') {
      setError('리뷰 내용을 입력해주세요.');
      return;
    }
    try {
      await axios.post(`/api/buyer/review/${orderId}`, 
        { rating, content },
        { headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` } }
      );
      onClose(true);
    } catch (err) {
      setError('리뷰 등록에 실패했습니다. 다시 시도해주세요.');
      console.error(err);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  };

  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay} onClick={() => onClose(false)}>
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2>{mode === 'create' ? '리뷰 작성' : '리뷰 확인'}</h2>
          <button className={styles.closeButton} onClick={() => onClose(false)}>×</button>
        </div>
        <div className={styles.modalBody}>
          <div className={styles.ratingSection}>
            <label>별점</label>
            <select value={rating} onChange={(e) => setRating(Number(e.target.value))} disabled={mode === 'view'}>
              <option value={5}>★★★★★ (5점)</option>
              <option value={4}>★★★★☆ (4점)</option>
              <option value={3}>★★★☆☆ (3점)</option>
              <option value={2}>★★☆☆☆ (2점)</option>
              <option value={1}>★☆☆☆☆ (1점)</option>
            </select>
          </div>
          <div className={styles.contentSection}>
            <label>리뷰 내용</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="리뷰를 작성해주세요."
              readOnly={mode === 'view'}
            />
          </div>
          {mode === 'view' && reviewData?.createdAt && (
            <div className={styles.createdAtSection}>
              <strong>작성일:</strong> {formatDate(reviewData.createdAt)}
            </div>
          )}
          {error && <p className={styles.errorText}>{error}</p>}
        </div>
        <div className={styles.modalFooter}>
          {mode === 'create' && (
            <button className={styles.submitButton} onClick={handleSubmit}>리뷰 등록</button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;