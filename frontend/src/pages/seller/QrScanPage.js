import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useZxing } from 'react-zxing';
import { DecodeHintType, BarcodeFormat } from '@zxing/library';
import styles from '../../styles/seller/QrScanPage.module.css';

// const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080';

const FRONT_RE = /(front|user|내장|내부|webcam|전면)/i;
const BACK_RE  = /(back|rear|environment|후면|외부)/i;
const BASE_URL = process.env.REACT_APP_API_URL || '';

const stopTracksOf = (videoEl) => {
    try {
        const ms = videoEl?.srcObject;
        if (ms && typeof ms.getTracks === 'function') {
            ms.getTracks().forEach((t) => t.stop());
        }
    } catch (_) {}
};

/** 장치가 있을 때만 마운트되는 스캐너(훅을 이 컴포넌트 내부에서만 사용) */
const ZxingScanner = ({
                          deviceId,
                          mirror,
                          onDecode,
                          onCameraError,
                      }) => {
    const videoElRef = useRef(null);

    const baseVideo = {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        advanced: [{ focusMode: 'continuous' }],
    };

    const constraints = deviceId
        ? { video: { ...baseVideo, deviceId: { exact: deviceId } }, audio: false }
        : { video: { ...baseVideo, facingMode: { ideal: 'user' } }, audio: false };

    const hints = useMemo(() => new Map([
        [DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.QR_CODE]],
        [DecodeHintType.TRY_HARDER, true],
    ]), []);

    const { ref: zxingVideoRef } = useZxing({
        onResult: (result) => onDecode?.(result),
        onError: onCameraError,
        constraints,
        hints,
    });

    const setVideoRef = useCallback((el) => {
        if (videoElRef.current && videoElRef.current !== el) stopTracksOf(videoElRef.current);
        videoElRef.current = el;
        if (zxingVideoRef && typeof zxingVideoRef === 'object') {
            zxingVideoRef.current = el;
        }
    }, [zxingVideoRef]);

    return (
        <video
            key={deviceId || 'default'}
            ref={setVideoRef}
            style={{
                width: '100%',
                aspectRatio: '16/10',
                objectFit: 'cover',
                background: '#000',
                transform: mirror ? 'scaleX(-1)' : 'none',
            }}
            muted
            playsInline
            autoPlay
        />
    );
};

const QrScanPage = () => {
    const { martId } = useParams();
    const [params] = useSearchParams();
    const navigate = useNavigate();

    const orderId = useMemo(() => Number(params.get('orderId')), [params]);

    const [orderDetails, setOrderDetails] = useState(null);

    const [status, setStatus] = useState('ready');
    const [message, setMessage] = useState('카메라 준비됨. QR을 비춰주세요.');
    const [devices, setDevices] = useState([]);
    const [deviceId, setDeviceId] = useState(null);
    const [mirror, setMirror]   = useState(true);
    const [hasPermission, setHasPermission] = useState(false);

    const lockRef = useRef(false);

    useEffect(() => {
        if (!orderId) return;

        const fetchOrderDetails = async () => {
            try {
                // GET 요청으로 API 호출
                // const response = await fetch(`${API_BASE}/api/qrCode/product?orderId=${orderId}`);
                const response = await fetch(`${BASE_URL}/api/qrCode/product?orderId=${orderId}`);
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status} - 서버에서 데이터를 가져오지 못했습니다.`);
                }
                const data = await response.json();
                setOrderDetails(data); // 받아온 데이터로 state 업데이트
            } catch (error) {
                console.error("주문 정보 로딩 실패:", error);
                setMessage(`주문 정보 로딩 실패: ${error.message}`);
            }
        };

        fetchOrderDetails();
    }, [orderId]); // orderId가 있을 때 한 번만 실행

    const refreshDevices = useCallback(async () => {
        const all = await navigator.mediaDevices.enumerateDevices().catch(() => []);
        const cams = all.filter((d) => d.kind === 'videoinput');
        setDevices(cams);

        const stillThere = cams.some((c) => c.deviceId === deviceId);
        let chosen = deviceId;
        if (!stillThere) {
            const back  = cams.find((d) => BACK_RE.test(d.label));
            const front = cams.find((d) => FRONT_RE.test(d.label));
            chosen = back?.deviceId || front?.deviceId || cams[0]?.deviceId || null;
            setDeviceId(chosen);
            setMirror(front ? true : false);
        } else {
            const label = cams.find(c => c.deviceId === chosen)?.label || '';
            setMirror(FRONT_RE.test(label));
        }

        if (!cams.length) {
            setMessage('사용 가능한 카메라가 없습니다. [카메라 권한 요청]을 눌러 권한을 허용하거나 장치를 확인해 주세요.');
        } else {
            setMessage('카메라 준비됨. QR을 비춰주세요.');
        }
    }, [deviceId]);

    useEffect(() => {
        (async () => {
            try {
                await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'user' } }, audio: false });
                setHasPermission(true);
            } catch {
                setHasPermission(false);
            } finally {
                await refreshDevices();
            }
        })();
    }, [refreshDevices]);

    useEffect(() => {
        (async () => {
            if (!deviceId) return;
            try {
                const ms = await navigator.mediaDevices.getUserMedia({ video: { deviceId: { exact: deviceId } }, audio: false });
                ms.getTracks().forEach(t => t.stop());
            } catch {}
        })();
    }, [deviceId]);

    const askPermissionAndRefresh = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: { ideal: 'user' } },
                audio: false,
            });
            setHasPermission(true);
            stream.getTracks().forEach(t => t.stop());
        } catch (err) {
            setHasPermission(false);
            setMessage(`권한 요청 실패: ${err?.message || err}`);
        } finally {
            await refreshDevices();
        }
    };

    const parseQrText = (text) => {
        try { const obj = JSON.parse(text); if (obj.uuid) return obj.uuid; } catch (_) {}
        const norm = String(text).replace(/[|]/g, '&').replace(/:/g, '=');
        const search = new URLSearchParams(norm);
        const uid = search.get('uuid');
        if (uid) return uid;
        if (/^[A-Za-z0-9\-_]{6,}$/.test(String(text).trim())) return String(text).trim();
        return null;
    };

    const sendCheckIn = useCallback(async (uuid) => {
        if (!orderId) {
            setStatus('error');
            setMessage('orderId가 없습니다.');
            lockRef.current = false;
            return;
        }
        setStatus('sending');
        setMessage('서버에 판매 확정 요청 중…');
        try {
            // const res = await fetch(`${API_BASE}/api/seller/orders/${orderId}/checkin`, {
            const res = await fetch(`${BASE_URL}/api/seller/orders/${orderId}/checkin`, {
                method: 'PATCH',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ uuid }),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            setStatus('success');
            setMessage('체크인 완료! (판매완료 처리 및 재고차감 완료)');
            setTimeout(() => navigate(`/seller/mart/${martId}/customers`), 900);
        } catch (err) {
            setStatus('ready');
            setMessage(`체크인 실패: ${err.message}`);
            lockRef.current = false;
        }
    }, [orderId, martId, navigate]);

    const handleDecode = useCallback((result) => {
        if (lockRef.current) return;
        const text = result?.getText?.() ?? '';
        const uuid = parseQrText(text);
        if (!uuid) {
            setMessage('QR 코드에서 uuid를 찾을 수 없습니다.');
            return;
        }

        console.log('스캔된 UUID 값:', uuid);

        lockRef.current = true;
        setStatus('scanning');
        setMessage(`QR 인식됨 → ${uuid}`);

        if (window.confirm('판매확정을 하시겠습니까?')) {
            sendCheckIn(uuid);
        } else {
            setStatus('ready');
            setMessage('카메라 준비됨. QR을 비춰주세요.');
            lockRef.current = false;
        }
    }, [sendCheckIn]);

    const handleCameraError = useCallback(async (error) => {
        const name = String(error?.name || '');
        const msg  = String(error?.message || error);
        if (/No MultiFormat Readers/i.test(msg)) return;
        if (/NotFoundError|OverconstrainedError/i.test(name + msg)) {
            setDeviceId(null);
            await refreshDevices();
            setMessage('선택한 카메라에 접근할 수 없어 기본 카메라로 전환했습니다.');
            return;
        }
        if (/play the video/i.test(msg)) {
            setMessage('카메라 영상을 재생하지 못했습니다. 다른 앱/탭이 카메라를 점유 중인지 확인해 주세요.');
            return;
        }
        setMessage(`카메라 오류: ${msg}`);
    }, [refreshDevices]);

    const hasAnyCamera = devices.length > 0;

    const pickFront = () => {
        const f = devices.find((d) => FRONT_RE.test(d.label)) || devices[0];
        if (f) { setDeviceId(f.deviceId); setMirror(true); }
    };
    const pickBack = () => {
        const b = devices.find((d) => BACK_RE.test(d.label)) || devices[0];
        if (b) { setDeviceId(b.deviceId); setMirror(false); }
    };
    const onDeviceChange = (id) => {
        setDeviceId(id || null);
        const label = devices.find((d) => d.deviceId === id)?.label || '';
        setMirror(FRONT_RE.test(label));
    };

    return (
        <div className={styles.page}>
            <main className={styles.container}>
                <header className={styles.header}>
                    <Link to={-1} className={styles.backButton}>← 뒤로</Link>
                    <h2 className={styles.title}>QR 스캔 (체크인)</h2>
                </header>

                <div className={styles.orderInfoContainer}>
                    {orderDetails ? (
                        <>
                            <div className={styles.orderInfoRow}>
                                <span>상품명 : </span>
                                <strong>{orderDetails.productName}</strong>
                            </div>
                            <div className={styles.orderInfoRow}>
                                <span>수량 : </span>
                                <strong>{orderDetails.quantity.toLocaleString('ko-KR')}개</strong>
                            </div>
                            <div className={styles.orderInfoRow}>
                                <span>결제 금액 : </span>
                                <strong>{orderDetails.totalPrice.toLocaleString('ko-KR')}원</strong>
                            </div>
                            <div className={styles.orderInfoRow}>
                                <span>결제 상태 : </span>
                                <strong className={orderDetails.status === 3 ? styles.statusComplete : styles.statusNeeded}>
                                    {orderDetails.status === 3 ? '결제 완료' : '결제필요'}
                                </strong>
                            </div>
                        </>
                    ) : (
                        <p className={styles.orderInfo}>주문 정보 로딩 중...</p>
                    )}
                </div>

                <div className={styles.controls}>
                    <div className={styles.buttonGroup}>
                        <button onClick={pickFront} disabled={!hasAnyCamera} className={styles.controlButton}>전면</button>
                        <button onClick={pickBack}  disabled={!hasAnyCamera} className={styles.controlButton}>후면</button>
                        <button onClick={() => setMirror(m => !m)} disabled={!hasAnyCamera} className={styles.controlButton}>미러 토글</button>
                        <button onClick={askPermissionAndRefresh} className={styles.controlButton}>카메라 권한 요청</button>
                    </div>
                    <label className={styles.cameraSelectWrapper}>
                        카메라:
                        <select
                            value={deviceId || ''}
                            onChange={(e) => onDeviceChange(e.target.value)}
                            disabled={!hasAnyCamera}
                            className={styles.cameraSelect}
                        >
                            {devices.map((d) => (
                                <option key={d.deviceId} value={d.deviceId}>
                                    {d.label || `Camera ${d.deviceId.slice(0, 4)}…`}
                                </option>
                            ))}
                            {!hasAnyCamera && <option value="">(카메라 없음)</option>}
                        </select>
                    </label>
                </div>

                <div className={styles.videoContainer}>
                    {hasAnyCamera ? (
                        <ZxingScanner
                            deviceId={deviceId}
                            mirror={mirror}
                            onDecode={handleDecode}
                            onCameraError={handleCameraError}
                        />
                    ) : (
                        <div className={styles.noCameraPlaceholder}>
                            {hasPermission ? '카메라를 찾지 못했습니다.' : '카메라 권한이 필요합니다.'}
                        </div>
                    )}
                </div>

                <div className={styles.statusBox}>
                    <div><b>상태:</b> {status}</div>
                    <div>{message}</div>
                </div>
            </main>
        </div>
    );
};

export default QrScanPage;
