import React, { useState, useEffect } from 'react';
import ProductInfoStyles from '../../styles/buyer/ProductInfo.module.css';
import { useAuth } from '../../contexts/AuthContext';
import axiosConfig from '../../services/axiosConfig';

const StoreInfo = ({ store, onShowMap }) => {
  const { isLoggedIn } = useAuth();

  const [isFollowing, setIsFollowing] = useState(store?.isFollowing || false);
  const [followerCount, setFollowerCount] = useState(store?.followersAmount || 0);

  useEffect(() => {
    setIsFollowing(store?.isFollowing || false);
    setFollowerCount(store?.followersAmount || 0);
  }, [store]);

  if (!store) {
    return null;
  }

  const formattedRating = store.averageRating ? store.averageRating.toFixed(2) : '0.00';

  const handleFollowToggle = async () => {
    if (!isLoggedIn) {
      alert('로그인이 필요한 기능입니다.');
      return;
    }

    const originalIsFollowing = isFollowing;
    const originalFollowerCount = followerCount;

    setIsFollowing(!originalIsFollowing);
    setFollowerCount(prevCount => originalIsFollowing ? prevCount - 1 : prevCount + 1);

    const endpoint = originalIsFollowing
      ? `/api/buyer/stores/unfollowStore/${store.storeId}`
      : `/api/buyer/stores/followStore/${store.storeId}`;

    try {
      await axiosConfig.post(endpoint);
    } catch (error) {
      console.error('Follow/Unfollow 요청 실패:', error);
      alert('요청에 실패했습니다. 다시 시도해주세요.');
      setIsFollowing(originalIsFollowing);
      setFollowerCount(originalFollowerCount);
    }
  };

  return (
    <div className={ProductInfoStyles["store-info-wrapper"]}>
      <p className={ProductInfoStyles["store-title"]}>매장 정보</p>
      <div className={ProductInfoStyles["store-info-layout"]}>
        <div className={ProductInfoStyles["store-main-content"]}>
          <div className={ProductInfoStyles["store-header"]}>
            <p className={ProductInfoStyles["store-name"]}>{store.name}</p>

            {isLoggedIn && (
              <button
                className={ProductInfoStyles["follow-btn"]}
                onClick={handleFollowToggle}
                style={{ backgroundColor: isFollowing ? '#888' : '#ffce2e' }}
              >
                {isFollowing ? '팔로잉' : '팔로우'}
              </button>
            )}
          </div>

          <p className={ProductInfoStyles["store-stats"]}>
            평점 {formattedRating} · 팔로워 {followerCount} · 후기 {store.reviewsAmount}
          </p>
        </div>

        <div className={ProductInfoStyles["store-address-content"]}>
          <p className={ProductInfoStyles["store-address-title"]}>매장주소</p>
          <p
            className={`${ProductInfoStyles["store-address"]} ${ProductInfoStyles["clickable-address"]}`}
            onClick={onShowMap}
          >
            {store.address}
          </p>
        </div>
      </div>
    </div>
  );
};

export default StoreInfo;
