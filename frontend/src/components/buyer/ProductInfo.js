import React, { useState } from 'react';
import ProductInfoStyles from '../../styles/buyer/ProductInfo.module.css';
import PaymentModal from './PaymentModal';
import { useAuth } from '../../contexts/AuthContext';
import loginApi from '../../services/loginApi';

const formatDate = (dateString) => {
    if (!dateString) return '알 수 없음';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
};

const ProductInfo = ({ product }) => {
    const [isAdding, setIsAdding] = useState(false);
    const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
    const [selectedQuantity, setSelectedQuantity] = useState(1);
    
    const { isLoggedIn } = useAuth();

    if (!product) {
        return null;
    }

    const handleIncrement = () => {
        const currentQuantity = Number(selectedQuantity);
        if (currentQuantity < product.quantity) {
            setSelectedQuantity(currentQuantity + 1);
        } else {
            alert(`최대 ${product.quantity}개까지 구매 가능합니다.`);
        }
    };

    const handleDecrement = () => {
        const currentQuantity = Number(selectedQuantity);
        if (currentQuantity > 1) {
            setSelectedQuantity(currentQuantity - 1);
        }
    };

    const handleQuantityChange = (e) => {
        const value = e.target.value;
        if (value === '' || /^[0-9\b]+$/.test(value)) {
            setSelectedQuantity(value);
        }
    };

    const handleBlur = () => {
        let quantity = Number(selectedQuantity);
        if (isNaN(quantity) || quantity < 1) {
            quantity = 1;
        } else if (quantity > product.quantity) {
            quantity = product.quantity;
            alert(`최대 ${product.quantity}개까지 구매 가능합니다.`);
        }
        setSelectedQuantity(quantity);
    };

    const handleAddToCart = async () => {
        if (!isLoggedIn) {
            alert('장바구니에 등록하기 전에 로그인을 해주세요.');
            return;
        }

        setIsAdding(true);
        try {
            await loginApi.post('/api/buyer/carts/addCart', {
                productId: product.productId,
                quantity: Number(selectedQuantity),
            });
            alert('장바구니에 추가되었습니다.');
        } catch (error) {
            console.error('장바구니 추가 실패:', error);
            alert('장바구니에 추가하는 데 실패했습니다.');
        } finally {
            setIsAdding(false);
        }
    };

    const handleBuyNowClick = () => {
        if (!isLoggedIn) {
            alert('로그인 후 구매 가능합니다.');
            return;
        }

        if (product.store && typeof product.store.paymentMethod !== 'undefined') {
            setIsPaymentModalOpen(true);
        } else {
            alert('결제 정보를 불러올 수 없습니다.');
        }
    };

    return (
        <>
            <div className={ProductInfoStyles["product-info"]}>
                <div>
                    <h1 className={ProductInfoStyles["product-name"]}>{product.name}</h1>
                    <p className={ProductInfoStyles["product-price"]}>{product.price.toLocaleString()}원</p>
                </div>
                <div className={ProductInfoStyles["info-row"]}>
                    <p className={ProductInfoStyles["product-expiry"]}>유통기한: {formatDate(product.expirationDate)}</p>
                    <div className={ProductInfoStyles["quantity-selector"]}>
                        <button onClick={handleDecrement} disabled={Number(selectedQuantity) <= 1}>-</button>
                        <input
                            type="text"
                            className={ProductInfoStyles["quantity-input"]}
                            value={selectedQuantity}
                            onChange={handleQuantityChange}
                            onBlur={handleBlur}
                        />
                        <button onClick={handleIncrement} disabled={Number(selectedQuantity) >= product.quantity}>+</button>
                    </div>
                </div>
                <div className={ProductInfoStyles["button-group"]}>
                    <button className={ProductInfoStyles["product-buy-btn"]} onClick={handleBuyNowClick}>
                        구매하기
                    </button>
                    <button className={ProductInfoStyles["product-cart-btn"]} onClick={handleAddToCart} disabled={isAdding}>
                        {isAdding ? '추가 중...' : '장바구니'}
                    </button>
                </div>
            </div>

            {product.store && (
                <PaymentModal
                    isOpen={isPaymentModalOpen}
                    onClose={() => setIsPaymentModalOpen(false)}
                    product={product}
                    quantity={selectedQuantity}
                />
            )}
        </>
    );
};

export default ProductInfo;