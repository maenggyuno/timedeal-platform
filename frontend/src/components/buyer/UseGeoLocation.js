import { useState, useEffect, useMemo, useCallback } from "react";

const UseGeoLocation = (autoRequest = false) => {
    const [location, setLocation] = useState({
        loaded: false,
        coordinates: { lat: "", lng: "" },
        isPermissionGranted: null
    });
    
    const [isLoading, setIsLoading] = useState(false); // 좌표를 가져오는 중
    const [isPermissionLoading, setIsPermissionLoading] = useState(true);

    const onSuccess = useCallback((location) => {
        setLocation({
            loaded: true,
            coordinates: {
                lat: location.coords.latitude,
                lng: location.coords.longitude,
            },
            isPermissionGranted: true
        });
        localStorage.setItem('locationPermission', 'granted');
        setIsLoading(false);
    }, []);

    const onError = useCallback((error) => {
        const isPermissionDenied = error.code === 1;
        setLocation({
            loaded: true,
            error: {
                code: error.code,
                message: error.message,
            },
            // 권한 거부 에러(error.code === 1)일 때만 false로 설정
            isPermissionGranted: isPermissionDenied ? false : null
        });
        if (isPermissionDenied) {
            localStorage.setItem('locationPermission', 'denied');
        }
        setIsLoading(false);
    }, []);

    const requestLocation = useCallback(() => {
        if (!("geolocation" in navigator)) {
            onError({
                code: 0,
                message: "Geolocation not supported",
            });
            return;
        }
        setIsLoading(true);
        navigator.geolocation.getCurrentPosition(onSuccess, onError, {
            enableHighAccuracy: false,
            timeout: 5000,
            maximumAge: 0
        });
    }, [onSuccess, onError]);

    useEffect(() => {
        if (!("permissions" in navigator)) {
            // 구형 브라우저 (permissions API 미지원)
            setIsPermissionLoading(false);
            if (autoRequest) {
                requestLocation();
            }
            return;
        }

        navigator.permissions.query({ name: 'geolocation' }).then(permissionStatus => {
            
            // 1. 이미 허용된 경우
            if (permissionStatus.state === 'granted') {
                setLocation(prev => ({ ...prev, isPermissionGranted: true }));
                if (autoRequest) {
                    requestLocation(); // 이미 허용됐으니 바로 위치 요청
                }
            // 2. 이미 거부된 경우
            } else if (permissionStatus.state === 'denied') {
                setLocation(prev => ({ ...prev, isPermissionGranted: false }));
                localStorage.setItem('locationPermission', 'denied');
            // 3. 아직 묻지 않은 경우 (prompt)
            } else {
                setLocation(prev => ({ ...prev, isPermissionGranted: null })); // 'false'가 아닌 'null'로 설정
                localStorage.removeItem('locationPermission');
            }
            
            setIsPermissionLoading(false); 

            permissionStatus.onchange = () => {
                // 상태 변경 시 로직
                if (permissionStatus.state === 'granted') {
                    window.location.reload();
                } else if (permissionStatus.state === 'denied') {
                    setLocation(prev => ({ ...prev, isPermissionGranted: false }));
                    localStorage.setItem('locationPermission', 'denied');
                } else {
                    setLocation(prev => ({ ...prev, isPermissionGranted: null }));
                    localStorage.removeItem('locationPermission');
                }
            };
        });
    }, [autoRequest, requestLocation]);

    const value = useMemo(() => ({
        location: location.coordinates,
        permissionGranted: location.isPermissionGranted,
        isPermissionLoading,
        error: location.error,
        requestLocation,
        isLoading,
    }), [location, isLoading, requestLocation, isPermissionLoading]);

    return value;
};

export default UseGeoLocation;