import React, { useEffect, useRef, useState } from 'react';

const NaverMap = ({ latitude, longitude }) => {
    const mapRef = useRef(null);
    // 스크립트 로딩 상태를 관리할 state 추가
    const [isScriptLoaded, setIsScriptLoaded] = useState(false);

    // 1. 스크립트를 로딩하는 useEffect (한 번만 실행됨)
    useEffect(() => {
        // 이미 스크립트가 로드되었다면 다시 로드하지 않음
        if (window.naver && window.naver.maps) {
            setIsScriptLoaded(true);
            return;
        }

        const script = document.createElement('script');
        script.type = 'text/javascript';
        const clientId = process.env.REACT_APP_NAVER_MAPS_CLIENT_ID;
        script.src = `https://openapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${clientId}`;
        script.async = true;
        script.onload = () => {
            setIsScriptLoaded(true);
        };
        script.onerror = () => console.error('네이버 맵스 스크립트 로드 실패');
        document.head.appendChild(script);
    }, []);

    // 2. 지도를 생성하는 useEffect (스크립트 로딩이 완료되고, 좌표가 있을 때만 실행)
    useEffect(() => {
        // 스크립트가 로드되지 않았거나, 위도/경도 값이 없거나, div가 준비되지 않았으면 실행하지 않음
        if (!isScriptLoaded || !latitude || !longitude || !mapRef.current) {
            return;
        }

        // 모든 조건이 충족되면 지도를 생성
        const mapOptions = {
            center: new window.naver.maps.LatLng(latitude, longitude),
            zoom: 17,
            scaleControl: false,
            logoControl: false,
            mapDataControl: false,
            zoomControl: true,
        };

        const map = new window.naver.maps.Map(mapRef.current, mapOptions);

        new window.naver.maps.Marker({
            position: new window.naver.maps.LatLng(latitude, longitude),
            map: map
        });

    }, [isScriptLoaded, latitude, longitude]);

    return (
        <div
            ref={mapRef}
            style={{
                width: '100%',
                height: '400px',
                backgroundColor: '#f7f7f7'
            }}
        />
    );
};

export default NaverMap;
