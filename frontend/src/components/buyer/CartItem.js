import React from 'react';
import styles from '../../styles/buyer/CartItem.module.css';

const CartItem = ({ item, onSelect, onIncrease, onDecrease, onQuantityChange, onQuantityBlur, onStoreClick, onDeleteItem }) => {
    
    // 상점 삭제 여부에 따라 적용할 클래스를 결정
    const itemClassName = `${styles.cartItem} ${item.storeIsDeleted ? styles.deletedStoreItem : ''}`;

    return (
        <div className={itemClassName}>
            {/* 상점이 삭제되었을 때 경고 메시지를 띄움 */}
            {item.storeIsDeleted && (
                <div className={styles.deletedStoreMessage}>
                    <div className={styles.messageText}>현재 상품의 상점은 폐쇄되었습니다.</div>
                    <button 
                        onClick={() => onDeleteItem(item.productId)} 
                        className={styles.deleteButton}
                    >
                        삭제
                    </button>
                </div>
            )}
            <div className={styles.checkboxContainer}>
                <input
                    type="checkbox"
                    checked={item.selected}
                    onChange={onSelect}
                    className={styles.checkbox}
                    disabled={item.storeIsDeleted}
                />
            </div>
            <div className={styles.imageContainer}>
                <img
                    src={item.productImgSrc || '/no-image.png'}
                    alt={item.productName}
                    className={styles.productImage}
                />
            </div>
            <div className={styles.productInfo}>
                <a 
                    className={styles.storeLink} 
                    onClick={() => !item.storeIsDeleted && onStoreClick({ 
                        storeId: item.storeId, 
                        storeName: item.storeName, 
                        storeAddress: item.storeAddress 
                    })}
                >
                    {item.storeName}
                </a>
                <h3 className={styles.productName}>{item.productName}</h3>
                <div className={styles.priceQuantityContainer}>
                    <p className={styles.productPrice}>{(item.price * Number(item.cartQuantity || 0)).toLocaleString()}원</p>
                    <div className={styles.quantityControl}>
                        <button 
                            onClick={onDecrease} 
                            className={styles.quantityButton}
                            disabled={Number(item.cartQuantity) <= 1 || item.storeIsDeleted}
                        >
                            -
                        </button>
                        
                        <input
                            type="text"
                            className={styles.quantityInput}
                            value={item.cartQuantity}
                            onChange={onQuantityChange}
                            onBlur={onQuantityBlur}
                            readOnly={item.storeIsDeleted}
                        />

                        <button 
                            onClick={onIncrease} 
                            className={styles.quantityButton} 
                            disabled={Number(item.cartQuantity) >= item.stockQuantity || item.storeIsDeleted}
                        >
                            +
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartItem;