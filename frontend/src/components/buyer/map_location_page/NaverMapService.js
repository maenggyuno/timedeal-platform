import axios from 'axios';

const NaverMapService = {
    /**
     * 특정 좌표 주변의 상점 정보를 백엔드로부터 가져옵니다.
     * @param {number} lat 위도
     * @param {number} lng 경도
     * @returns 상점 정보 배열
     */
    fetchNearbyStores: async (lat, lng) => {
        try {
            // /api/buyer/maps/nearby 엔드포인트에 lat, lng 파라미터를 전달합니다.
            const response = await axios.get(`/api/buyer/maps/nearbyStores?lat=${lat}&lng=${lng}`);
            return response.data;
        } catch (error) {
            console.error("주변 상점 정보를 불러오는데 실패했습니다.", error);
            throw error;
        }
    },

    loadNaverMapsScript: () => {
        if (window.naver && window.naver.maps) {
            return Promise.resolve();
        }

        const clientId = process.env.REACT_APP_NAVER_MAPS_CLIENT_ID;

        if (!clientId) {
            return Promise.reject(new Error("네이버 지도 클라이언트 ID가 설정되지 않았습니다."));
        }

        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = `https://openapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${clientId}&submodules=geocoder`;
            script.async = true;
            script.onload = resolve;
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }
};

export default NaverMapService;