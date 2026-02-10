import React, { useEffect, useState, useRef, useCallback } from 'react';
import axiosConfig from '../../../services/axiosConfig';
import mapInfoModalStyles from '../../../styles/buyer/MapInfoModal.module.css';
import ModalProductItem from './ModalProductItem';

const PRODUCTS_PER_PAGE = 12;

const MapInfoModal = ({ isOpen, onClose, locationInfo }) => {
    const modalRef = useRef(null);
    const [products, setProducts] = useState([]);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchProducts = useCallback((pageNum) => {
        if (!locationInfo?.storeId) return;

        setIsLoading(true);
        setError(null);

        axiosConfig.get(`/api/buyer/products/${locationInfo.storeId}/products?page=${pageNum}&size=${PRODUCTS_PER_PAGE}`)
            .then(response => {
                const newProducts = response.data;
                setProducts(prev => pageNum === 0 ? newProducts : [...prev, ...newProducts]);
                setHasMore(newProducts.length === PRODUCTS_PER_PAGE);
            })
            .catch(err => {
                setError("상품 정보를 불러오는 데 실패했습니다.");
                console.error(`Error fetching store products:`, err);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [locationInfo?.storeId]);

    useEffect(() => {
        if (isOpen) {
            // 모달이 열릴 때 상태 초기화 및 첫 페이지 로드
            setProducts([]);
            setPage(0);
            setHasMore(true);
            fetchProducts(0);
        }
    }, [isOpen, fetchProducts]);

    const handleLoadMore = () => {
        const nextPage = page + 1;
        setPage(nextPage);
        fetchProducts(nextPage);
    };

    if (!isOpen) return null;

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div className={mapInfoModalStyles["map-modal-overlay"]} onClick={handleOverlayClick}>
            <div className={mapInfoModalStyles["map-modal-content"]} ref={modalRef}>
                <div className={mapInfoModalStyles["map-modal-header"]}>
                    <h2>{locationInfo.name}</h2>
                    <button className={mapInfoModalStyles["modal-close-btn"]} onClick={onClose}>×</button>
                </div>
                <div className={mapInfoModalStyles["map-modal-body"]}>
                    <div className={mapInfoModalStyles["info-item"]}><strong>주소:</strong><p>{locationInfo.address}</p></div>
                    <div className={mapInfoModalStyles["info-item"]}><strong>연락처:</strong><p>{locationInfo.phoneNumber}</p></div>
                    <div className={mapInfoModalStyles["info-item"]}><strong>영업시간:</strong><p>{locationInfo.businessHours}</p></div>

                    <hr className={mapInfoModalStyles["divider"]} />

                    <h3 className={mapInfoModalStyles["products-title"]}>판매중인 상품</h3>

                    <div className={mapInfoModalStyles["modal-product-grid"]}>
                        {products.map(product => (
                            <ModalProductItem key={product.productId} product={product} />
                        ))}
                    </div>

                    {isLoading && <p className={mapInfoModalStyles["loading-text"]}>상품 목록을 불러오는 중...</p>}
                    {error && <p className={mapInfoModalStyles["error-text"]}>{error}</p>}
                    {!isLoading && !error && products.length === 0 && <p className={mapInfoModalStyles["loading-text"]}>판매중인 상품이 없습니다.</p>}

                    {hasMore && !isLoading && (
                        <div className={mapInfoModalStyles["more-button-container"]}>
                            <button
                                className={mapInfoModalStyles["more-button"]}
                                onClick={handleLoadMore}
                                disabled={isLoading}
                            >
                                더보기
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MapInfoModal;
