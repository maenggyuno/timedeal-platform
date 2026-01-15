import React from 'react';
import modalStyles from '../../../styles/buyer/MapInfoModal.module.css';

const ModalProductItem = ({ product }) => {
  const imageUrl = product.imageUrl || '/no-image.png';    

  return (
    <a 
      href={`/buyer/product/${product.productId}`} 
      className={modalStyles["modal-product-item"]}
      target="_blank" 
      rel="noopener noreferrer" 
    >
      <div className={modalStyles["modal-product-image-container"]}>
        <img src={imageUrl} alt={product.name} className={modalStyles["modal-product-image"]} />
      </div>
      <div className={modalStyles["modal-product-info"]}>
        <div className={modalStyles["modal-product-name"]}>{product.name}</div>
        <div className={modalStyles["modal-product-price"]}>{product.price.toLocaleString()}원</div>
      </div>
    </a>
  );
};

export default ModalProductItem;