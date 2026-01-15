import React, { useEffect, useRef, useState, useCallback } from 'react';
import MapInfoModal from './MapInfoModal';
import NaverMapService from './NaverMapService';
import UseGeoLocation from '../UseGeoLocation';
import locationMapStyles from '../../../styles/buyer/LocationMap.module.css';
import ProductStyles from '../../../styles/buyer/Product.module.css';
import { MdMyLocation, MdRefresh } from "react-icons/md";
import { LuPlus, LuMinus } from "react-icons/lu";

const LocationMap = () => {
    const mapRef = useRef(null);
    const mapInstance = useRef(null);
    const [markers, setMarkers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedStore, setSelectedStore] = useState(null);

    const {
        location: userLocation,
        permissionGranted,
        requestLocation,
        isLoading: isLocationLoading,
        error: locationError,
    } = UseGeoLocation(true);

    // 마커를 지도에서 제거하는 함수
    const clearMarkers = useCallback(() => {
        markers.forEach(marker => marker.setMap(null));
        setMarkers([]);
    }, [markers]);

    // 특정 위치를 기준으로 상점을 검색하고 마커를 표시하는 함수
    const searchAndDisplayStores = useCallback(async (lat, lng) => {
        if (!mapInstance.current) return;

        setIsLoading(true);
        setError(null);
        clearMarkers();

        try {
            const stores = await NaverMapService.fetchNearbyStores(lat, lng);
            if (!stores || stores.length === 0) {
                // alert 대신 setError를 사용해 메시지를 표시
                setError("주변 5km 내에 검색된 상점이 없습니다.");
                // 1초 후에 메시지를 자동으로 지움
                setTimeout(() => setError(null), 1000);
            }

            const newMarkers = stores.map(store => {
                const marker = new window.naver.maps.Marker({
                    position: new window.naver.maps.LatLng(store.latitude, store.longitude),
                    map: mapInstance.current,
                });
                window.naver.maps.Event.addListener(marker, 'click', () => {
                    setSelectedStore(store);
                    setIsModalOpen(true);
                });
                return marker;
            });
            setMarkers(newMarkers);

        } catch (err) {
            setError("상점 정보를 불러오는 중 오류가 발생했습니다.");
        } finally {
            setIsLoading(false);
        }
    }, [clearMarkers]);

    // 지도 초기화 로직
    useEffect(() => {
        const initMap = async () => {
            if (!permissionGranted || !userLocation.lat || !mapRef.current || mapInstance.current) {
                return;
            }
            try {
                await NaverMapService.loadNaverMapsScript();
                const startLocation = new window.naver.maps.LatLng(userLocation.lat, userLocation.lng);
                const mapOptions = { center: startLocation, zoom: 16, zoomControl: false };
                const map = new window.naver.maps.Map(mapRef.current, mapOptions);
                mapInstance.current = map;

                // 초기 위치 기준으로 상점 검색
                searchAndDisplayStores(userLocation.lat, userLocation.lng);

            } catch (err) {
                setError("지도 초기화 중 오류가 발생했습니다.");
            }
        };
        initMap();
    }, [permissionGranted, userLocation, searchAndDisplayStores]);

    // '다시 검색' 버튼 핸들러
    const handleResearch = () => {
        if (!mapInstance.current) return;
        const center = mapInstance.current.getCenter();
        searchAndDisplayStores(center.lat(), center.lng());
    };

    // 현재 위치로 이동
    const moveToCurrentLocation = () => {
        if (userLocation.lat && mapInstance.current) {
            const currentLocation = new window.naver.maps.LatLng(userLocation.lat, userLocation.lng);
            mapInstance.current.panTo(currentLocation, { duration: 500 });
        }
    };

    // 줌인 핸들러
    const handleZoomIn = () => {
        if (mapInstance.current) {
            mapInstance.current.setZoom(mapInstance.current.getZoom() + 1);
        }
    };

    // 줌아웃 핸들러
    const handleZoomOut = () => {
        if (mapInstance.current) {
            mapInstance.current.setZoom(mapInstance.current.getZoom() - 1);
        }
    };

    return (
        <div className={locationMapStyles["location-map-container"]}>
            {permissionGranted ? (
                <div className={locationMapStyles["location-map-wrapper"]}>
                    <div className={locationMapStyles["map-controls"]}>
                        <button onClick={moveToCurrentLocation} className={locationMapStyles["map-control-btn"]} title="현재 위치로">
                            <MdMyLocation />
                        </button>
                        <button onClick={handleZoomIn} className={locationMapStyles["map-control-btn"]} title="확대">
                            <LuPlus />
                        </button>
                        <button onClick={handleZoomOut} className={locationMapStyles["map-control-btn"]} title="축소">
                            <LuMinus />
                        </button>
                        <button onClick={handleResearch} className={locationMapStyles["map-control-btn"]} title="여기서 다시 검색">
                            <MdRefresh />
                        </button>
                    </div>

                    {isLocationLoading && <p className={locationMapStyles["map-loading"]}>위치 정보를 찾는 중...</p>}
                    {isLoading && <p className={locationMapStyles["map-loading"]}>상점 정보를 불러오는 중...</p>}
                    {locationError && <p className={locationMapStyles["map-error"]}>위치 정보를 가져올 수 없습니다: {locationError.message}</p>}
                    {error && <p className={locationMapStyles["map-error"]}>{error}</p>}
                    
                    <div ref={mapRef} className={locationMapStyles["location-map"]}></div>
                    
                    {selectedStore && (
                        <MapInfoModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} locationInfo={selectedStore} />
                    )}
                </div>
            ) : (
                <section className={ProductStyles["location-section"]}>
                    <div className={ProductStyles["location-denied-message"]}>
                        <p>지도를 보려면 위치 정보 접근이 필요합니다.</p>
                        <button className={ProductStyles["enable-location-button"]} onClick={requestLocation} disabled={isLocationLoading}>
                            {isLocationLoading ? '위치 정보 확인 중...' : '위치 정보 접근 허용하기'}
                        </button>
                    </div>
                </section>
            )}
        </div>
    );
};

export default LocationMap;