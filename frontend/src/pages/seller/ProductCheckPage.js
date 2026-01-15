import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import styles from '../../styles/seller/ProductCheckPage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';
import Sidebar from '../../components/seller/Sidebar';

import { fetchProductStatusSummary, adjustProductStock } from '../../services/sellerApi';

const TABS = ['판매대기', '판매중', '판매완료'];

const ProductCheckPage = () => {
  const { martId } = useParams();

  const [productSummaries, setProductSummaries] = useState([]);
  const [selectedTab, setSelectedTab] = useState('판매대기');
  const [loading, setLoading] = useState(true);
  const [loadingId, setLoadingId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const loadSummary = async () => {
      setLoading(true);
      try {
        const { data } = await fetchProductStatusSummary(martId);
        setProductSummaries(data || []);
      } catch (e) {
        console.error(e);
        alert('상품 현황 데이터를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    if (martId) {
      loadSummary();
    }
  }, [martId]);

  // 탭 + 검색 필터링
  const visibleProducts = useMemo(() => {
      if (!productSummaries) return [];
  
      let productsByTab = [];
      switch (selectedTab) {
        case '판매대기':
          productsByTab = productSummaries.filter(p => p.waitingCount > 0);
          break;
        case '판매중(예약)':
          productsByTab = productSummaries.filter(p => p.inSaleCount > 0);
          break;
        case '판매완료':
          productsByTab = productSummaries.filter(p => p.completedCount > 0);
          break;
        default:
          productsByTab = [];
      }
  
      if (searchQuery.trim() === '') {
        return productsByTab;
      }
      
      return productsByTab.filter(p =>
          p.productName.toLowerCase().includes(searchQuery.toLowerCase().trim())
      );
    }, [productSummaries, selectedTab, searchQuery]);

  const handleStockChange = async (product, delta) => {
    if (!delta || delta === 0) return;
    if (selectedTab !== '판매대기') {
      alert('재고 조정은 [판매대기] 탭에서만 가능합니다.');
      return;
    }

    if (!window.confirm(`"${product.productName}" 재고를 ${delta > 0 ? `+${delta}` : delta} 만큼 변경할까요? (판매대기 수량만 변경됩니다)`)) {
      return;
    }

    setLoadingId(product.productId);

    try {
      await adjustProductStock(martId, product.productId, delta);
      setLoading(true);
      try {
        const { data } = await fetchProductStatusSummary(martId);
        setProductSummaries(data || []);
      } catch (e) {
        console.error(e);
        alert('상품 현황 데이터를 다시 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
      
    } catch (e) {
      console.error(e);
      alert('재고 변경에 실패했습니다.');
    } finally {
      setLoadingId(null);
    }
  };

  const renderProductRow = (product) => {
    let quantityText = '';
    switch (selectedTab) {
      case '판매대기':
        quantityText = `${product.waitingCount}개 (판매 가능)`;
        break;
      case '판매중':
        quantityText = `${product.inSaleCount}개 (예약됨)`;
        break;
      case '판매완료':
        quantityText = `${product.completedCount}개 (판매 완료)`;
        break;
      default:
        break;
    }

    return (
        <tr key={product.productId}>
          <td className={styles.imageCell}>
            {product.productImgSrc ? (
                <img src={product.productImgSrc} alt={product.productName} className={styles.thumb} referrerPolicy="no-referrer" />
            ) : (
                <span className={styles.thumbPlaceholder}>이미지 없음</span>
            )}
          </td>
          <td>{product.productName}</td>
          <td>
            <div className={styles.quantityCell}>
              <span>{quantityText}</span>
              {selectedTab === '판매대기' && (
                <div className={styles.buttonGroup}>
                  <button
                      className={`${styles.stockButton} ${styles.increaseButton}`}
                      disabled={loadingId === product.productId}
                      onClick={() => handleStockChange(product, +1)}
                  >
                    추가
                  </button>
                  <button
                      className={`${styles.stockButton} ${styles.decreaseButton}`}
                      disabled={loadingId === product.productId}
                      onClick={() => handleStockChange(product, -1)}
                  >
                    감소
                  </button>
                </div>
              )}
            </div>
          </td>
        </tr>
    );
  };

  return (
      <>
        <Header />
        <Sidebar base={`/seller/mart/${martId}`} />
        <div className={styles.page}>
          <div className={styles.content}>
            <div className={styles.headerRow}>
              <div className={styles.titleAndTabs}>
                <h2 className={styles.title}>등록 상품 현황</h2>
                <div className={styles.tabsWrap}>
                  <div className={styles.tabs}>
                    {TABS.map(tab => (
                        <button
                            key={tab}
                            className={`${styles.tab} ${selectedTab === tab ? styles.tabActive : ''}`}
                            onClick={() => setSelectedTab(tab)}
                        >
                          {tab}
                        </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className={styles.searchContainer}>
              <input
                  type="text"
                  className={styles.searchInput}
                  placeholder="상품명으로 검색..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                <tr>
                  <th>상품이미지</th>
                  <th>상품명</th>
                  <th>수량 현황</th>
                </tr>
                </thead>
                <tbody>
                {loading ? (
                    <tr><td colSpan="3">데이터를 불러오는 중입니다...</td></tr>
                ) : (
                    visibleProducts.length > 0 ?
                        visibleProducts.map(renderProductRow) :
                        <tr>
                          <td colSpan="3">
                            {searchQuery ? "검색 결과가 없습니다." : "해당 상태의 상품이 없습니다."}
                          </td>
                        </tr>
                )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <Footer />
      </>
  );
};

export default ProductCheckPage;