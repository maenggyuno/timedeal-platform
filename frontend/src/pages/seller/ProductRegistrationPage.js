import React, { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { registerProduct, getPresignedUrl, uploadToPresignedUrl } from '../../services/sellerApi';
import styles from '../../styles/seller/ProductRegistrationPage.module.css';
import Footer from '../../components/seller/Footer';
import Header from '../../components/seller/Header';
import Sidebar from '../../components/seller/Sidebar';


const ProductRegistrationPage = () => {
  const navigate = useNavigate();
  const { martId } = useParams();

  const CATEGORY_GROUPS = useMemo(() => ({
    '신선식품': ['과일', '채소', '육류', '수산물', '유제품'],
    '가공식품': ['통조림/병조림', '즉석식품', '소스/양념', '간편조리식'],
    '건강/특수식품': ['유기농/친환경', '비건/채식', '다이어트/저칼로리'],
    '베이커리/디저트': ['빵', '케이크/파이', '쿠키/스낵'],
    '음료': ['생수/탄산수', '주스/스무디', '커피/차', '기능성 음료'],
  }), []);
  const MAIN_CATS = Object.keys(CATEGORY_GROUPS);

  const [mainCategory, setMainCategory] = useState(MAIN_CATS[0]);
  const [subCategory, setSubCategory] = useState(CATEGORY_GROUPS[MAIN_CATS[0]][0]);

  const [productName, setProductName] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  const [expirationDate, setExpirationDate] = useState('');
  const [expirationTime, setExpirationTime] = useState('');
  const [description, setDescription] = useState('');

  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState('');

  const handleMainChange = (e) => {
    const nextMain = e.target.value;
    setMainCategory(nextMain);
    setSubCategory(CATEGORY_GROUPS[nextMain][0]);
  };
  const handleSubChange = (e) => setSubCategory(e.target.value);
  const handleFileChange = (e) => {
    const f = e.target.files?.[0];
    setFile(f || null);
    setPreview(f ? URL.createObjectURL(f) : '');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const isoDateTime = expirationDate
        ? `${expirationDate}T${expirationTime || '00:00'}:00`
        : '';

    let productImgSrc = '';

    try {
      if (file) {
        const contentType = file.type || 'application/octet-stream';
        const presignRes = await getPresignedUrl(file.name, contentType);
        const { url, key, publicUrl } = presignRes.data;

        const putRes = await uploadToPresignedUrl(url, file, contentType);
        if (!putRes.ok) throw new Error('S3 업로드 실패');

        productImgSrc = publicUrl || key;
      }

      const payload = {
        category: subCategory,
        productName,
        price: Number(price),
        quantity: Number(quantity),
        expirationDate: isoDateTime,
        description,
        productImgSrc,
      };

      await registerProduct(martId, payload);
      navigate(`/seller/mart/${martId}/check`);
    } catch (err) {
      console.error(err);
      alert('상품 등록 중 오류가 발생했습니다.');
    }
  };

  return (
      <div className={styles.pageContainer}>
        <Header />
        <Sidebar base={`/seller/mart/${martId}`} />

        <main className={styles.mainContent}>
          <h2 className={styles.sectionTitle}>상품 등록</h2>

          <form className={styles.productForm} onSubmit={handleSubmit}>
            <div className={styles.imageSection}>
              <label className={styles.imagePlaceholder}>
                {preview ? (
                    <img src={preview} alt="미리보기" className={styles.previewImg} />
                ) : (
                    <span>이미지 선택</span>
                )}
                <input type="file" accept="image/*" onChange={handleFileChange} style={{ display: 'none' }} />
              </label>
            </div>

            <div className={styles.formFields}>
              <div className={styles.formGroup}>
                <label htmlFor="mainCat">대분류</label>
                <select id="mainCat" className={styles.formInput} value={mainCategory} onChange={handleMainChange} required>
                  {MAIN_CATS.map((mc) => (<option key={mc} value={mc}>{mc}</option>))}
                </select>
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="subCat">소분류</label>
                <select id="subCat" className={styles.formInput} value={subCategory} onChange={handleSubChange} required>
                  {CATEGORY_GROUPS[mainCategory].map((sc) => (<option key={sc} value={sc}>{sc}</option>))}
                </select>
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="name">상품명</label>
                <input id="name" type="text" className={styles.formInput} value={productName} onChange={e => setProductName(e.target.value)} required />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="price">가격</label>
                <div className={styles.inputWithUnit}>
                  <input id="price" type="number" className={styles.formInput} value={price} onChange={e => setPrice(e.target.value)} required />
                  <span className={styles.unit}>원</span>
                </div>
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="quantity">수량</label>
                <div className={styles.inputWithUnit}>
                  <input id="quantity" type="number" className={styles.formInput} value={quantity} onChange={e => setQuantity(e.target.value)} required />
                  <span className={styles.unit}>개</span>
                </div>
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="expDate">판매 종료일</label>
                <input id="expDate" type="date" className={styles.formInput} value={expirationDate} onChange={e => setExpirationDate(e.target.value)} required />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="expTime">종료 시간</label>
                <input id="expTime" type="time" className={styles.formInput} value={expirationTime} onChange={e => setExpirationTime(e.target.value)} required />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="desc">상품설명</label>
                <textarea id="desc" className={styles.formTextarea} value={description} onChange={e => setDescription(e.target.value)} rows={4} />
              </div>

              <div className={styles.buttonContainer}>
                <button type="submit" className={styles.submitButton}>등록</button>
              </div>
            </div>
          </form>
        </main>

        <Footer />
      </div>
  );
};

export default ProductRegistrationPage;
