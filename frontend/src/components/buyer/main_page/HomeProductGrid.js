import React, { useState, useEffect, useCallback } from 'react';
import HomeProductItem from './HomeProductItem';
import ProductStyles from '../../../styles/buyer/Product.module.css';
import UseGeoLocation from '../UseGeoLocation';

const PRODUCTS_PER_PAGE = 30;

const HomeProductGrid = () => {
  const [products, setProducts] = useState([]); 
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);
  
  const {
    location = {},
    permissionGranted,
    isPermissionLoading,
    requestLocation,
    isLoading: isLocationLoading
  } = UseGeoLocation(true);

  // 진단 코드 1: 렌더링이 발생할 때마다 현재 상태를 출력
  console.log('--- 렌더링 시작 ---', { isPermissionLoading, permissionGranted });

  const fetchProducts = useCallback((pageNum) => {
    if (!location.lat || !location.lng) return;

    setIsLoading(true);
    fetch(`/api/buyer/products/nearby?lat=${location.lat}&lng=${location.lng}&distance=3&page=${pageNum}&size=${PRODUCTS_PER_PAGE}`)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
      })
      .then(newProducts => {
        setProducts(prev => pageNum === 0 ? newProducts : [...prev, ...newProducts]);
        setHasMore(newProducts.length === PRODUCTS_PER_PAGE);
      })
      .catch(error => console.error("Error fetching nearby products:", error))
      .finally(() => {
        setIsLoading(false);
        if (pageNum === 0) setInitialLoadComplete(true);
      });
  }, [location.lat, location.lng]);

  useEffect(() => {
    if (permissionGranted && location.lat && location.lng) {
        setPage(0);
        setProducts([]);
        fetchProducts(0);
    }
  }, [permissionGranted, location.lat, location.lng, fetchProducts]);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchProducts(nextPage);
  };

  // 1. 권한을 확인하는 중 (isPermissionLoading === true)
  if (isPermissionLoading) {
    console.log('[UI] "권한 확인 중..." 화면 표시');
    return (
      <section className={ProductStyles["location-section"]}>
        <div className={ProductStyles["loading-message"]}>
          <p>위치 권한을 확인하고 있습니다...</p>
        </div>
      </section>
    );
  }

  // 2. 권한이 거부된 경우 (permissionGranted === false)
  if (permissionGranted === false) {
    console.log('[UI] "권한 거부됨" 화면 표시');
    return (
      <section className={ProductStyles["location-section"]}>
        <div className={ProductStyles["location-denied-message"]}>
          <p>위치 정보 접근이 거부되었습니다.</p>
          <p>주변 상품을 보려면, 휴대폰의 [브라우저 설정] 내 [사이트 설정]에서 <strong>dongnekok.pics</strong>의 위치 권한을 '허용'으로 변경해주세요.</p>
        </div>
      </section>
    );
  }
  
  // 3. 권한을 요청해야 하는 경우 (permissionGranted === null)
  if (permissionGranted === null) {
    console.log('[UI] "권한 필요" 화면 표시 (버튼)');
    return (
      <section className={ProductStyles["location-section"]}>
        <div className={ProductStyles["location-denied-message"]}>
          <p>주변 상품을 보려면 위치 정보 접근이 필요합니다.</p>
          <button 
            className={ProductStyles["enable-location-button"]}
            onClick={requestLocation} 
            disabled={isLocationLoading}
          >
            {isLocationLoading ? '위치 정보 확인 중...' : '위치 정보 접근 허용하기'}
          </button>
        </div>
      </section>
    );
  }
  
  // 4. 모든 확인을 통과한 경우 (permissionGranted === true)
  // 진단 코드 4: 어떤 UI가 그려지는지 확인
  console.log('[UI] 정상 상품 목록 표시');
  return (
    <section className={ProductStyles["product-section"]}>
      <h2 className={ProductStyles["section-title"]}>내 주변 상품</h2>
      <div className={ProductStyles["product-grid"]}>
        {products.map((product) => (
          <HomeProductItem key={product.productId} product={product} />
        ))}
      </div>
      
      {initialLoadComplete && !isLoading && products.length === 0 && (
        <div className={ProductStyles["no-products-message"]}>
          <p>주변에 등록된 상품이 없거나, 운영중인 상점이 없습니다.</p>
        </div>
      )}

      {isLoading && <div className={ProductStyles["loading"]}></div>}

      {hasMore && !isLoading && products.length > 0 && (
        <div className={ProductStyles["more-button-container"]}>
          <button
            className={ProductStyles["more-button"]}
            onClick={handleLoadMore}
            disabled={isLoading}
          >
            더보기
          </button>
        </div>
      )}
    </section>
  );
};

export default HomeProductGrid;