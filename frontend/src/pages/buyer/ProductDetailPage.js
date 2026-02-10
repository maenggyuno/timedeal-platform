import React, {useState, useEffect} from 'react';
import {useParams} from 'react-router-dom';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import ProductInfo from '../../components/buyer/ProductInfo';
import StoreInfo from '../../components/buyer/StoreInfo';
import StoreLocationModal from '../../components/buyer/product_info_store_location/StoreLocationModal';
import ProductInfoStyles from '../../styles/buyer/ProductInfo.module.css';
import axiosConfig from '../../services/axiosConfig';

const ProductDetailPage = () => {
  const [product, setProduct] = useState(null);
  const [isStoreModalOpen, setIsStoreModalOpen] = useState(false);
  const {productId} = useParams();

  useEffect(() => {
    if (productId) {
      axiosConfig.get(`/api/buyer/products/${productId}`)
        .then(response => {
          // axios는 .json() 변환 필요 없음! 바로 .data 쓰면 됨
          setProduct(response.data);
        })
        .catch(error => {
          console.error("Error fetching product:", error);
        });
    }
  }, [productId]);

  if (!product) {
    return <div>상품 정보를 불러오는 중입니다...</div>;
  }

  const imageUrl = product.imageUrl || '/no-image.png';

  return (
    <>
      <Header/>
      <main>
        <div className={ProductInfoStyles.container}>
          {/* 왼쪽: 상품 이미지 영역 */}
          <div className={ProductInfoStyles["left-area"]}>
            <img src={imageUrl} alt={product.name} className={ProductInfoStyles["product-image"]}/>
          </div>
          {/* 오른쪽: 상품 정보 및 결제 영역 */}
          <div className={ProductInfoStyles["right-area"]}>
            <ProductInfo product={product}/>
            <hr className={ProductInfoStyles.divider}/>
            <div className={ProductInfoStyles["product-description-section"]}>
              <p className={ProductInfoStyles["description-content"]}>
                {product.description}
              </p>
            </div>
          </div>
        </div>

        {/* 하단: 매장 정보 영역 */}
        <div className={ProductInfoStyles["store-section-container"]}>
          <StoreInfo store={product.store} onShowMap={() => setIsStoreModalOpen(true)}/>
        </div>

        {/* 매장 위치 정보 모달 */}
        {product.store && (
          <StoreLocationModal
            isOpen={isStoreModalOpen}
            onClose={() => setIsStoreModalOpen(false)}
            // ✨ storeId를 반드시 전달해야 합니다.
            storeId={product.store.storeId}
            storeName={product.store.name}
            storeAddress={product.store.address}
          />
        )}
      </main>
      <Footer/>
    </>
  );
};

export default ProductDetailPage;
