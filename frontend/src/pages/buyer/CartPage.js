import React, { useState, useEffect } from 'react';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import CartItem from '../../components/buyer/CartItem';
import styles from '../../styles/buyer/Cart.module.css';
import StoreLocationModal from '../../components/buyer/product_info_store_location/StoreLocationModal';
import CartPaymentModal from '../../components/buyer/CartPaymentModal';

const CartPage = () => {
    const [cartItems, setCartItems] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectAll, setSelectAll] = useState(true);
    const [selectedStore, setSelectedStore] = useState(null);
    const [isCartModalOpen, setIsCartModalOpen] = useState(false);
    const [itemsToPay, setItemsToPay] = useState([]);

    useEffect(() => {
        fetch('/api/buyer/carts/all', {
            credentials: 'include'
        })
        .then(res => res.json())
        .then(data => {
            const itemsWithSelection = data.map(item => ({
                productId: item.productId,
                productName: item.productName,
                price: item.productPrice,
                storeName: item.storeName,
                productImgSrc: item.productImageUrl,
                cartQuantity: item.cartProductQuantity,
                stockQuantity: item.productQuantity,
                selected: !item.storeIsDeleted, 
                storeId: item.storeId,
                storeAddress: item.storeAddress,
                storePaymentMethod: item.storePaymentMethod,
                storeIsDeleted: item.storeIsDeleted,
            }));
            setCartItems(itemsWithSelection);
            
            const allSelectableItems = itemsWithSelection.filter(item => !item.storeIsDeleted);
            if (allSelectableItems.length > 0) {
                setSelectAll(allSelectableItems.every(item => item.selected));
            } else {
                setSelectAll(false);
            }
        })
        .catch(error => console.error("장바구니 조회 에러:", error))
        .finally(() => setIsLoading(false));
    }, []);

    const handleSelectAll = () => {
        const newSelectAll = !selectAll;
        setSelectAll(newSelectAll);
        setCartItems(cartItems.map(item => 
            item.storeIsDeleted ? { ...item, selected: false } : { ...item, selected: newSelectAll }
        ));
    };

    const handleSelectItem = (productId) => {
        const newItems = cartItems.map(item =>
            item.productId === productId ? { ...item, selected: !item.selected } : item
        );
        setCartItems(newItems);
        
        const allSelectableItems = newItems.filter(item => !item.storeIsDeleted);
        if (allSelectableItems.length > 0) {
            setSelectAll(allSelectableItems.every(item => item.selected));
        } else {
            setSelectAll(false);
        }
    };

    const handleIncrease = (productId) => {
        setCartItems(cartItems.map(item => {
            if (item.productId === productId) {
                const currentQuantity = Number(item.cartQuantity);
                if (currentQuantity < item.stockQuantity) {
                    return { ...item, cartQuantity: currentQuantity + 1 };
                } else {
                    alert('재고 수량을 초과할 수 없습니다.');
                    return item;
                }
            }
            return item;
        }));
    };

    const handleDecrease = (productId) => {
        setCartItems(cartItems.map(item => {
            const currentQuantity = Number(item.cartQuantity);
            return item.productId === productId && currentQuantity > 1
                   ? { ...item, cartQuantity: currentQuantity - 1 }
                   : item;
        }));
    };

    const handleQuantityChange = (productId, event) => {
        const value = event.target.value;
        if (value === '' || /^[0-9\b]+$/.test(value)) {
            setCartItems(cartItems.map(item =>
                item.productId === productId ? { ...item, cartQuantity: value } : item
            ));
        }
    };

    const handleQuantityBlur = (productId) => {
        const targetItem = cartItems.find(item => item.productId === productId);
        if (!targetItem) return;

        let quantity = Number(targetItem.cartQuantity);

        if (isNaN(quantity) || quantity < 1) {
            quantity = 1;
        } else if (quantity > targetItem.stockQuantity) {
            quantity = targetItem.stockQuantity;
            alert('재고 수량을 초과할 수 없습니다.');
        }

        setCartItems(cartItems.map(item =>
            item.productId === productId ? { ...item, cartQuantity: quantity } : item
        ));
    };

    const handleStoreClick = (storeData) => {
        setSelectedStore(storeData);
    };

    const handleDeleteSelected = () => {
        const productIdsToDelete = cartItems
            .filter(item => item.selected)
            .map(item => item.productId);

        if (productIdsToDelete.length === 0) {
            alert("삭제할 상품을 선택해주세요.");
            return;
        }

        fetch('/api/buyer/carts', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ productIds: productIdsToDelete }),
        })
        .then(res => {
            if (!res.ok) {
                throw new Error('상품 삭제에 실패했습니다.');
            }
            setCartItems(prevItems => prevItems.filter(item => !item.selected));
            setSelectAll(false);
        })
        .catch(error => {
            console.error("선택삭제 중 에러 발생:", error);
            alert(error.message);
        });
    };

    // 개별 상품 삭제 핸들러
    const handleDeleteItem = (productId) => {
        if (!window.confirm("이 상품을 장바구니에서 삭제하시겠습니까?")) {
            return;
        }

        fetch('/api/buyer/carts', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ productIds: [productId] }),
        })
        .then(res => {
            if (!res.ok) {
                throw new Error('상품 삭제에 실패했습니다.');
            }
            // 상태 업데이트: 해당 상품 ID를 제외한 새 배열 생성
            setCartItems(prevItems => prevItems.filter(item => item.productId !== productId));
        })
        .catch(error => {
            console.error("개별 상품 삭제 중 에러 발생:", error);
            alert(error.message);
        });
    };

    const handlePurchaseClick = () => {
        const selectedItems = cartItems.filter(item => item.selected);
        if (selectedItems.length === 0) {
            alert('결제할 상품을 선택해주세요.');
            return;
        }
        
        const hasDeletedStoreItem = selectedItems.some(item => item.storeIsDeleted);
        if (hasDeletedStoreItem) {
            alert('구매할 수 없는 상품이 선택되었습니다. 폐쇄된 상점의 상품을 선택 해제해주세요.');
            return;
        }
        
        setItemsToPay(selectedItems);
        setIsCartModalOpen(true);
    };

    const totalAmount = cartItems
        .filter(item => item.selected)
        .reduce((sum, item) => sum + item.price * Number(item.cartQuantity || 0), 0);

    if (isLoading) {
        return (
             <div className={styles.cartWrapper}>
                <Header />
                <div className={styles.cartContainer}>
                    <h1 className={styles.cartTitle}>장바구니</h1>
                    <p>장바구니를 불러오는 중입니다...</p>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div className={styles.cartWrapper}>
            <Header />
            <div className={styles.cartContainer}>
                <br />
                <div className={styles.titleContainer}>
                    <h1 className={styles.cartTitle}>장바구니</h1>
                    <div className={styles.mobileSelectOptions}>
                        <button
                            className={`${styles.selectButton} ${selectAll ? styles.active : ''}`}
                            onClick={handleSelectAll}
                        >
                            전체선택
                        </button>
                        <button className={styles.selectButton} onClick={handleDeleteSelected}>
                            선택삭제
                        </button>
                    </div>
                </div>
                <div className={styles.cartActions}>
                    <div className={styles.desktopSelectOptions}>
                        <button
                            className={`${styles.selectButton} ${selectAll ? styles.active : ''}`}
                            onClick={handleSelectAll}
                        >
                            전체선택
                        </button>
                        <button className={styles.selectButton} onClick={handleDeleteSelected}>
                            선택삭제
                        </button>
                    </div>
                    <div className={styles.cartSummary}>
                        <p className={styles.totalAmount}>총 금액: {totalAmount.toLocaleString()}원</p>
                        <button className={styles.purchaseButton} onClick={handlePurchaseClick}>구매하기</button>
                    </div>
                </div>
                <div className={styles.cartItemsContainer}>
                    <div className={styles.cartItemsGrid}>
                        {cartItems.length > 0 ? (
                            cartItems.map(item => (
                                <CartItem
                                    key={item.productId}
                                    item={item}
                                    onSelect={() => handleSelectItem(item.productId)}
                                    onIncrease={() => handleIncrease(item.productId)}
                                    onDecrease={() => handleDecrease(item.productId)}
                                    onQuantityChange={(e) => handleQuantityChange(item.productId, e)}
                                    onQuantityBlur={() => handleQuantityBlur(item.productId)}
                                    onStoreClick={handleStoreClick}
                                    onDeleteItem={handleDeleteItem}
                                />
                            ))
                        ) : (
                            <p className={styles.emptyCartMessage}>장바구니에 담긴 상품이 없습니다.</p>
                        )}
                    </div>
                </div>
            </div>

            {selectedStore && (
                <StoreLocationModal
                    isOpen={!!selectedStore}
                    onClose={() => setSelectedStore(null)}
                    storeId={selectedStore.storeId}
                    storeName={selectedStore.storeName}
                    storeAddress={selectedStore.storeAddress}
                />
            )}
            
            <CartPaymentModal
                isOpen={isCartModalOpen}
                onClose={() => setIsCartModalOpen(false)}
                items={itemsToPay}
            />
            <Footer />
        </div>
    );
};

export default CartPage;