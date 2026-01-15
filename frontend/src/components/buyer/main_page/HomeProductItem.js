import React from 'react';
import { Link } from 'react-router-dom';
import ProductStyles from '../../../styles/buyer/Product.module.css';

const HomeProductItem = ({ product }) => {
  const imageUrl = product.imageUrl || '/no-image.png';

  return (
    <div className={ProductStyles["product-item"]}>
      <Link to={`/buyer/product/${product.productId}`}>
        <div className={ProductStyles["product-image-container"]}>
          <img src={imageUrl} alt={product.name} className={ProductStyles["product-image"]} />
        </div>
        <div className={ProductStyles["product-item-info"]}>
          <div className={ProductStyles["product-name"]}>{product.name}</div>
          <div className={ProductStyles["product-price"]}>{product.price?.toLocaleString()}원</div>
        </div>
      </Link>
    </div>
  );
};

export default HomeProductItem;