import React, { useState, useEffect } from 'react';
import axiosConfig from '../../../services/axiosConfig';
import NaverMap from './NaverMap';
import mapInfoModalStyles from '../../../styles/buyer/MapInfoModal.module.css';

const StoreLocationModal = ({ isOpen, onClose, storeId, storeName, storeAddress }) => {
    // 위도, 경도를 저장할 상태와 로딩/에러 상태를 추가
    const [location, setLocation] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        // 모달이 열리고, storeId가 있을 때만 API를 호출
        if (isOpen && storeId) {
            setIsLoading(true);
            setError(null);
            setLocation(null);

            axiosConfig().get(`/api/buyer/maps/${storeId}`)
                .then(response => {
                    setLocation(response.data);
                })
                .catch(err => {
                    setError("매장 위치 정보를 불러오는 데 실패했습니다.");
                    console.error("Error fetching store location:", err);
                })
                .finally(() => {
                    setIsLoading(false);
                });
        }
    }, [isOpen, storeId]);

    if (!isOpen) return null;

    return (
        <div className={mapInfoModalStyles["map-modal-overlay"]} onClick={onClose}>
            <div className={mapInfoModalStyles["map-modal-content"]} onClick={(e) => e.stopPropagation()}>
                <div className={mapInfoModalStyles["map-modal-header"]}>
                    <h2>{storeName}</h2>
                    <button className={mapInfoModalStyles["modal-close-btn"]} onClick={onClose}>×</button>
                </div>
                <div className={mapInfoModalStyles["map-modal-body"]}>
                    <div className={mapInfoModalStyles["info-item"]}>
                        <strong>주소:</strong>
                        <p>{storeAddress}</p>
                    </div>
                    {location && (
                        <>
                            <div className={mapInfoModalStyles["info-item"]}>
                                <strong>연락처:</strong>
                                <p>{location.phoneNumber}</p>
                            </div>
                            <div className={mapInfoModalStyles["info-item"]}>
                                <strong>운영시간:</strong>
                                <p>{location.businessHours}</p>
                            </div>
                        </>
                    )}

                    <div style={{ width: '100%', height: '400px', marginTop: '16px', backgroundColor: '#f7f7f7' }}>
                        {isLoading && <p style={{ textAlign: 'center', paddingTop: '180px' }}>지도를 불러오는 중...</p>}
                        {error && <p style={{ textAlign: 'center', paddingTop: '180px', color: 'red' }}>{error}</p>}
                        {location && location.latitude && location.longitude && (
                            <NaverMap latitude={location.latitude} longitude={location.longitude} />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StoreLocationModal;
