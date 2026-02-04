import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../../styles/seller/Main_Button.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';

const Main_StoreCreatePage = () => {
  // --- 상태 관리 ---
  const [storeName, setStoreName] = useState('');
  const [postcode, setPostcode] = useState('');
  const [baseAddress, setBaseAddress] = useState('');
  const [detailAddress, setDetailAddress] = useState('');
  const [fullAddress, setFullAddress] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [phone, setPhone] = useState('');
  const [businessNumber, setBusinessNumber] = useState('');
  const [openTime, setOpenTime] = useState('');
  const [closeTime, setCloseTime] = useState('');
  const [paymentMethod, setPaymentMethod] = useState(0);
  const [message, setMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [bizCheckState, setBizCheckState] = useState('idle');

  const navigate = useNavigate();
  const detailAddressRef = useRef(null);
  const tokenCache = useRef({ token: null, exp: 0 });

  // 👇 [전역 변수] 대문자로 통일하고 맨 위에 선언!
  const BASE_URL = process.env.REACT_APP_API_URL || '';

  // --- 스크립트 로드 ---
  useEffect(() => {
    const script = document.createElement('script');
    script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true;
    document.head.appendChild(script);
    return () => { document.head.removeChild(script); };
  }, []);
  //
  // // --- API 및 유틸리티 함수 ---
  // const getSgisToken = async () => {
  //   const now = Date.now();
  //   if (tokenCache.current.token && now < tokenCache.current.exp) {
  //     return tokenCache.current.token;
  //   }
  //   if (!SGIS_KEY || !SGIS_SECRET) {
  //     setMessage('SGIS API 키가 설정되지 않았습니다.');
  //     throw new Error('SGIS KEY/SECRET is not set');
  //   }
  //   const url = `${BASE_URL}/OpenAPI3/auth/authentication.json?consumer_key=${SGIS_KEY}&consumer_secret=${SGIS_SECRET}`;
  //   const res = await fetch(url);
  //   const data = await res.json();
  //   if (data.errCd !== 0 || !data?.result?.accessToken) {
  //     throw new Error(`SGIS 인증 실패: ${data.errMsg || '토큰 없음'}`);
  //   }
  //   const token = data.result.accessToken;
  //   tokenCache.current = { token, exp: now + 25 * 60 * 1000 };
  //   return token;
  // };
  //
  // const geocodeWGS84 = async (addr, retried = false) => {
  //   try {
  //     const token = await getSgisToken();
  //     const url =
  //       `${BASE_URL}/OpenAPI3/addr/geocodewgs84.json` +
  //       `?accessToken=${encodeURIComponent(token)}` +
  //       `&address=${encodeURIComponent(addr)}`;
  //     const res = await fetch(url);
  //     const data = await res.json();
  //     if (data.errCd !== 0) {
  //       if (data.errCd === -401 && !retried) {
  //         tokenCache.current = { token: null, exp: 0 };
  //         return geocodeWGS84(addr, true);
  //       }
  //       console.warn('[SGIS Geocoding Error]', data.errMsg);
  //       return null;
  //     }
  //     const firstResult = data?.result?.resultdata?.[0];
  //     return firstResult ? { lat: String(firstResult.y), lng: String(firstResult.x) } : null;
  //   } catch (error) {
  //     console.error("Geocoding failed", error);
  //     throw error;
  //   }
  // };
// [삭제] const getSgisToken = ... (이제 필요 없음!)

// [수정] 백엔드한테 "좌표 줘" 한 마디만 하면 끝
  const geocodeWGS84 = async (addr) => {
    try {
      // 1. 토큰? 키값? 다 필요 없음. 그냥 주소만 백엔드로 던짐.
      // 백엔드 주소: /api/sgis/geocode (아까 만든 Controller)
      const url = `${BASE_URL}/api/sgis/geocode?address=${encodeURIComponent(addr)}`;

      const res = await fetch(url);
      const data = await res.json();

      // 2. 에러 처리
      if (data.errCd && data.errCd !== 0) {
        console.warn('[SGIS Error]', data.errMsg);
        return null;
      }

      // 3. 결과 반환 (구조는 동일)
      const firstResult = data?.result?.resultdata?.[0];
      return firstResult ? { lat: String(firstResult.y), lng: String(firstResult.x) } : null;

    } catch (error) {
      console.error("Geocoding failed", error);
      throw error; // 필요하면 에러 다시 던지기
    }
  };


  useEffect(() => {
    const combinedAddress = baseAddress ? `${baseAddress}, ${detailAddress}` : '';
    setFullAddress(combinedAddress);
    if (!combinedAddress.trim()) {
      setLatitude(''); setLongitude(''); return;
    }
    const handler = setTimeout(async () => {
      try {
        const coords = await geocodeWGS84(baseAddress);
        if (coords) {
          setLatitude(coords.lat); setLongitude(coords.lng); setMessage('');
        } else {
          setLatitude(''); setLongitude(''); setMessage('주소 좌표를 찾을 수 없습니다.');
        }
      } catch (e) {
        setLatitude(''); setLongitude(''); setMessage('주소 변환 중 오류가 발생했습니다.');
      }
    }, 500);
    return () => clearTimeout(handler);
  }, [baseAddress, detailAddress]);

  useEffect(() => {
    setBizCheckState('idle');
  }, [businessNumber]);

  const handleOpenPostcode = () => {
    if (window.daum && window.daum.Postcode) {
      new window.daum.Postcode({
        oncomplete: (data) => {
          setPostcode(data.zonecode);
          setBaseAddress(data.address);
          detailAddressRef.current?.focus();
        },
      }).open();
    } else {
      setMessage('우편번호 서비스를 불러오는 중입니다.');
    }
  };

  const handleBusinessNumberChange = (e) => {
    const { value } = e.target;
    const onlyNums = value.replace(/[^0-9]/g, '');
    if (onlyNums.length <= 10) {
        setBusinessNumber(onlyNums);
    }
  };

  const handlePhoneChange = (e) => {
    const { value } = e.target;
    const onlyNumsAndHyphen = value.replace(/[^0-9-]/g, '');
    setPhone(onlyNumsAndHyphen);
  };

  const handleCheckBusinessNumber = async () => {
    const cleanedBno = businessNumber.replace(/[^0-9]/g, '');
    if (cleanedBno.length !== 10) {
      alert('사업자 등록번호는 숫자 10자리여야 합니다.');
      setBizCheckState('invalid');
      return;
    }
    try {
      setBizCheckState('checking');
      const res = await fetch(`${BASE_URL}/api/seller/tax/biz-status?bno=${cleanedBno}`);
      if (!res.ok) throw new Error(`API 오류(${res.status})`);
      const data = await res.json();
      const item = data?.data?.[0];
      if (!item) {
        alert('조회에 실패했습니다. 잠시 후 다시 시도하세요.');
        setBizCheckState('error');
        return;
      }

      // [수정] 'valid'가 아니면 모두 'invalid'로 처리
      if (item.b_stt_cd === '01') {
        setBizCheckState('valid');
        alert(`확인되었습니다. (과세유형: ${item.tax_type || '정보 없음'})`);
      } else {
        setBizCheckState('invalid');
        alert('유효하지 않은 사업자 등록번호입니다.');
      }

    } catch (e) {
      console.error(e);
      alert('조회 중 오류가 발생했습니다.');
      setBizCheckState('error');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!storeName || !fullAddress || !phone || !businessNumber || !openTime || !closeTime) {
      setMessage('필수 항목을 모두 입력해주세요.');
      return;
    }
    if (!latitude || !longitude) {
      setMessage('주소 좌표가 유효하지 않습니다. 주소를 다시 확인해주세요.');
      return;
    }
    setIsSubmitting(true);
    const payload = {
      name: storeName,
      address: fullAddress,
      latitude: Number(latitude),
      longitude: Number(longitude),
      phone_number: phone,
      business_number: businessNumber.replace(/[^0-9]/g, ''),
      opening_time: `${openTime}:00`,
      closing_time: `${closeTime}:00`,
      payment_method: paymentMethod,
    };
    try {
      const res = await fetch(`${BASE_URL}/api/seller/store/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(`매장 등록 실패 (${res.status}): ${errText}`);
      }
      const createdStore = await res.json();
      navigate('/seller/dashboard', { state: { successMessage: `'${createdStore.name}' 매장이 성공적으로 등록되었습니다.` } });
    } catch (err) {
      console.error(err);
      setMessage(err.message || '서버 통신 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Header />
      <div className={styles.container}>
        <h2>매장 생성</h2>
        <form onSubmit={handleSubmit} className={styles.form}>
          <label>매장명:
            <input type="text" value={storeName} onChange={(e) => setStoreName(e.target.value)} required />
          </label>

          <label>주소:</label>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input type="text" value={postcode} placeholder="우편번호" readOnly style={{ flex: '0 1 120px' }} />
            <button type="button" onClick={handleOpenPostcode} className={styles.smallButton}>
              우편번호 찾기
            </button>
          </div>
          <input type="text" value={baseAddress} placeholder="주소" readOnly style={{ marginTop: '8px', width: '100%' }} />
          <input ref={detailAddressRef} type="text" value={detailAddress} onChange={(e) => setDetailAddress(e.target.value)} placeholder="상세주소 입력" style={{ marginTop: '8px', width: '100%' }} required />

          <label>전화번호:
            <input type="tel" value={phone} onChange={handlePhoneChange} placeholder="예) 010-1234-5678" required />
          </label>

          <label>사업자등록번호:</label>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              type="text"
              value={businessNumber}
              onChange={handleBusinessNumberChange}
              onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleCheckBusinessNumber(); } }}
              placeholder="숫자 10자리 입력"
              style={{ flex: 1 }}
              required
              readOnly={bizCheckState === 'valid'}
            />
            <button type="button" onClick={handleCheckBusinessNumber} disabled={bizCheckState === 'checking' || bizCheckState === 'valid'} className={styles.smallButton}>
              {bizCheckState === 'valid' ? '확인완료' : (bizCheckState === 'checking' ? '확인중...' : '진위 확인')}
            </button>
          </div>

          <div style={{display: 'flex', gap: '1rem'}}>
            <label style={{flex: 1}}>오픈 시간:
              <input type="time" value={openTime} onChange={(e) => setOpenTime(e.target.value)} required />
            </label>
            <label style={{flex: 1}}>마감 시간:
              <input type="time" value={closeTime} onChange={(e) => setCloseTime(e.target.value)} required />
            </label>
          </div>

          <label>결제 방식:
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(Number(e.target.value))}
              required
            >
              <option value={0}>현장 결제</option>
              <option value={1}>현장 + 카드 결제</option>
            </select>
          </label>

          <div className={styles.buttonGroup}>
            <button type="submit" className={styles.submitButton} disabled={isSubmitting}>
              {isSubmitting ? '등록 중...' : '매장 등록하기'}
            </button>
            <button type="button" className={styles.cancelButton} onClick={() => navigate(-1)}>취소</button>
          </div>

          {message && <p className={styles.message}>{message}</p>}
        </form>
      </div>
      <Footer />
    </>
  );
};

export default Main_StoreCreatePage;
