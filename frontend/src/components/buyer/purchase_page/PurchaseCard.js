import React from 'react';
import cardStyles from '../../styles/buyer/PurchaseCard.module.css';

const PurchaseCard = ({ purchase, formatDate, formatPrice }) => {
    return (
        <div className={cardStyles.card}>
            <div className={cardStyles.cardContent}>
                <div className={cardStyles.imageContainer}>
                    <img
                        src={purchase.image}
                        alt={purchase.productName}
                        className={cardStyles.productImage}
                    />
                </div>
                <div className={cardStyles.infoContainer}>
                    <div className={cardStyles.mainInfo}>
                        <p className={cardStyles.sellerName}>{purchase.seller}</p>
                        <h3 className={cardStyles.productName}>{purchase.productName}</h3>
                        <p className={cardStyles.productPrice}>{formatPrice(purchase.price)}</p>
                    </div>
                    <div className={cardStyles.sideInfo}>
                        <p className={cardStyles.purchaseDate}>{formatDate(purchase.purchaseDate)}</p>
                        <span className={cardStyles.statusBadge}>
              {purchase.status}
            </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PurchaseCard;