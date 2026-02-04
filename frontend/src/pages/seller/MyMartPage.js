import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import styles from '../../styles/seller/MyMartPage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';
import NaverMapService from '../../components/buyer/map_location_page/NaverMapService';
import Sidebar from '../../components/seller/Sidebar';

const maskName = (name = '') => {
  const s = name.trim();
  if (!s) return '익명';
  if (s.length <= 2) return s[0] + '*';
  return s.slice(0, 2) + '★'.repeat(Math.max(1, s.length - 2));
};

const timeAgo = (iso) => {
  if (!iso) return '';
  const now = new Date(), t = new Date(iso);
  const diff = (now - t) / 1000, m = Math.floor(diff / 60), h = Math.floor(m / 60), d = Math.floor(h / 24);
  if (diff < 60) return `${Math.floor(diff)}초 전`;
  if (m < 60) return `${m}분 전`;
  if (h < 24) return `${h}시간 전`;
  if (d < 7) return `${d}일 전`;
  return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`;
};

/** 정수 별(0~5). 4.6 → 5개(반올림) */
const renderStars = (value) => {
  if (value == null) return '☆☆☆☆☆';
  const v = Math.round(Number(value));
  return '⭐'.repeat(v) + '☆'.repeat(5 - v);
};

const formatScore = (v) => (v == null ? '0.0' : Number(v).toFixed(1));
const fmtTime = (t) => {
  if (!t) return '';
  const [hh, mm] = t.split(':');
  return `${hh}:${mm}`;
};

const MyMartPage = () => {
  const { martId } = useParams();

  // 상단 마트 정보
  const [store, setStore] = useState(null);
  const [loadingStore, setLoadingStore] = useState(true);

  // 리뷰/요약
  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(true);
  const [errorReviews, setErrorReviews] = useState('');
  const [summary, setSummary] = useState({ avgAll: null, avg3m: null, cntAll: 0, cnt3m: 0 });

  // 모달
  const [isModalOpen, setModalOpen] = useState(false);
  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);

  // 지도 ref
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);

  // 배경 스크롤 잠금 + ESC 닫기
  useEffect(() => {
    if (!isModalOpen) return;
    const onKey = (e) => e.key === 'Escape' && setModalOpen(false);
    const prev = document.body.style.overflow;
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
    };
  }, [isModalOpen]);

  // 마트 상세 조회
  useEffect(() => {
    let ignore = false;
    (async () => {
      try {
        setLoadingStore(true);
        const response = await axios.get(`${BASE_URL}/api/seller/store/${martId}/mymart`);
        const apiData = response.data;
        if (!ignore) {
          const formattedData = {
            id: apiData.storeId,
            name: apiData.storeName,
            address: apiData.storeAddress,
            phone: apiData.phoneNumber,
            openTime: apiData.openTime,
            closeTime: apiData.closeTime,
            latitude: apiData.latitude,
            longitude: apiData.longitude,
          };
          setStore(formattedData);
        }
      } catch (e) {
        if (!ignore) setStore(null);
        console.error('마트 정보를 불러오는 중 오류', e);
      } finally {
        if (!ignore) setLoadingStore(false);
      }
    })();
    return () => { ignore = true; };
  }, [martId]);

  // 리뷰 불러오기
  useEffect(() => {
    let ignore = false;
    (async () => {
      try {
        setLoadingReviews(true);
        setErrorReviews('');
        const res = await fetch(`${BASE_URL}/api/seller/store/${martId}/reviews`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!ignore) setReviews(Array.isArray(data) ? data : []);
      } catch (e) {
        if (!ignore) setErrorReviews('리뷰를 불러오지 못했습니다.');
        console.error(e);
      } finally {
        if (!ignore) setLoadingReviews(false);
      }
    })();
    return () => { ignore = true; };
  }, [martId]);

  // 요약 평점
  useEffect(() => {
    let ignore = false;
    (async () => {
      try {
        const res = await fetch(`${BASE_URL}/api/seller/store/${martId}/review-summary`);
        if (!res.ok) return;
        const data = await res.json();
        if (!ignore) setSummary(data);
      } catch {}
    })();
    return () => { ignore = true; };
  }, [martId]);

  // 네이버 지도 초기화/갱신
  useEffect(() => {
    const lat = store?.latitude;
    const lng = store?.longitude;
    if (lat == null || lng == null || !mapRef.current) return;

    const initMap = async () => {
      try {
        await NaverMapService.loadNaverMapsScript();
        const storeLocation = new window.naver.maps.LatLng(lat, lng);

        if (!mapInstanceRef.current) {
          mapInstanceRef.current = new window.naver.maps.Map(mapRef.current, {
            center: storeLocation,
            zoom: 17,
            zoomControl: false,

            // --- 지도 상호작용 비활성화 옵션 ---
            draggable: false,              // 마우스 드래그 및 터치 이동 비활성화
            pinchZoom: false,              // 두 손가락 줌 비활성화
            scrollWheel: false,            // 마우스 휠 줌 비활성화
            keyboardShortcuts: false,      // 키보드 단축키 비활성화
            disableDoubleTapZoom: true,    // 더블탭 줌 비활성화
            disableDoubleClickZoom: true,  // 더블클릭 줌 비활성화
          });
        } else {
          mapInstanceRef.current.panTo(storeLocation, { duration: 500 });
        }

        if (markerRef.current) markerRef.current.setMap(null);
        markerRef.current = new window.naver.maps.Marker({
          position: storeLocation,
          map: mapInstanceRef.current,
        });
      } catch (error) {
        console.error('네이버 지도 초기화 실패', error);
      }
    };

    initMap();
  }, [store?.latitude, store?.longitude]);

  const overall  = (summary.cntAll ?? (reviews?.length ?? 0)) > 0 && summary.avgAll != null ? summary.avgAll : 0;
  const recent3m = (summary.cnt3m ?? 0) > 0 && summary.avg3m != null ? summary.avg3m : 0;

  return (
    <>
      <Header />
      <div className={styles['mart-dashboard-container']}>
        {/* 공용 사이드바 (본문 자동 밀기 pushContent=true) */}
        <Sidebar base={`/seller/mart/${martId}`} pushContent />

        {/* 본문 */}
        <div className={styles['mymart-container']}>
          <h2>{loadingStore ? '불러오는 중…' : (store?.name ?? `마트 ${martId}`)}</h2><br />
          <div className={styles.twoCol}>
            {/* 왼쪽 컬럼 */}
            <section className={styles.leftCol}>

              <div className={styles['mart-info']}>
                {/* 지도 */}
                <div className={styles['map-section']}>
                  {store?.latitude != null && store?.longitude != null ? (
                    <div ref={mapRef} style={{ width: '100%', height: '100%' }} />
                  ) : (
                    <div style={{display:'flex',alignItems:'center',justifyContent:'center',width:'100%',height:'100%',color:'#666'}}>
                      위치 정보가 없습니다
                    </div>
                  )}
                </div>

                {/* 상세 정보 */}
                <div className={styles['details']}>
                  <p>
                    <strong>주소:</strong>
                    <span className={styles.value} title={store?.address ?? ''}>
                      {store?.address ?? '-'}
                    </span>
                  </p>
                  <p>
                    <strong>연락처:</strong>
                    <span className={styles.value} title={store?.phone ?? ''}>
                      {store?.phone ?? '-'}
                    </span>
                  </p>
                  <p>
                    <strong>운영시간:</strong>
                    <span
                      className={styles.value}
                      title={
                        store?.openTime && store?.closeTime
                          ? `${fmtTime(store.openTime)} ~ ${fmtTime(store.closeTime)}`
                          : ''
                      }
                    >
                      {store?.openTime && store?.closeTime
                        ? `${fmtTime(store.openTime)} ~ ${fmtTime(store.closeTime)}`
                        : '-'}
                    </span>
                  </p>
                </div>
              </div>
            </section>

            {/* 오른쪽 컬럼 */}
            <aside className={styles.rightCol}>
              <div className={styles['ratings-reviews-container']}>
                {/* 평점 박스 */}
                <div className={styles['ratings']}>
                  <div className={styles['rating-box']}>
                    <h3>전체 평점</h3>
                    <p>
                      <span>{renderStars(overall)}</span>
                      <span style={{ marginLeft: 8 }}>{formatScore(overall)}점</span>
                      <span style={{ marginLeft: 6, color: '#777' }}>({summary.cntAll ?? 0}건)</span>
                    </p>
                  </div>
                  <div className={styles['rating-box']}>
                    <h3>최근 3개월 평점 평균</h3>
                    <p>
                      <span>{renderStars(recent3m)}</span>
                      <span style={{ marginLeft: 8 }}>{formatScore(recent3m)}점</span>
                      <span style={{ marginLeft: 6, color: '#777' }}>({summary.cnt3m ?? 0}건)</span>
                    </p>
                  </div>
                </div>

                {/* 리뷰 카드 */}
                <div className={styles['reviews']}>
                  <div className={styles['reviewsHeader']}>
                    <h3>상품 리뷰</h3>
                    {reviews.length > 0 && (
                      <button className={styles['viewAllBtn']} onClick={openModal}>
                        전체 리뷰 보기 ({summary.cntAll ?? reviews.length})
                      </button>
                    )}
                  </div>

                  <div className={styles.reviewList}>
                    {loadingReviews && <p>리뷰를 불러오는 중…</p>}
                    {errorReviews && <p style={{ color: 'crimson' }}>{errorReviews}</p>}
                    {!loadingReviews && !errorReviews && reviews.length === 0 && <p>아직 등록된 리뷰가 없습니다.</p>}
                    {reviews.map((rv) => (
                      <div key={rv.reviewId} className={styles['review']}>
                        <p>
                          <strong>{maskName(rv.userName)}</strong> {' | '}
                          <span>{renderStars(rv.stars)}</span> {' | '}
                          {timeAgo(rv.createdAt)}
                        </p>
                        <p>{rv.text}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </aside>
          </div>
        </div>
      </div>

      {/* 리뷰 모달 */}
      {isModalOpen && (
        <div className={styles.modalOverlay} onClick={closeModal} role="dialog" aria-modal="true">
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>전체 리뷰</h3>
              <button className={styles.modalClose} onClick={closeModal} aria-label="닫기">×</button>
            </div>
            <div className={styles.modalBody}>
              {reviews.map((rv) => (
                <div key={rv.reviewId} className={styles.modalReviewItem}>
                  <div className={styles.modalReviewMeta}>
                    <strong>{maskName(rv.userName)}</strong>
                    <span className={styles.stars}>{renderStars(rv.stars)}</span>
                    <span className={styles.time}>{timeAgo(rv.createdAt)}</span>
                  </div>
                  <div className={styles.modalReviewText}>{rv.text}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <Footer />
    </>
  );
};

export default MyMartPage;
