import React, { useState, useEffect } from 'react';
import api from '../services/axiosConfig'; // Axios 설정 파일 경로
import MaintenancePage from './MaintenancePage';

const MaintenanceGuard = ({ children }) => {
  const [isServerLive, setIsServerLive] = useState(null); // null: 확인 중

  useEffect(() => {
    const checkServerStatus = async () => {
      try {
        // 1. 루트 경로('') 헬스 체크
        await api.head('', {
          timeout: 3000, // 3초 타임아웃
        });

        // 성공(200 OK)하면 생존 확인
        setIsServerLive(true);

      } catch (error) {
        // ⚠️ 중요: 서버가 404(Not Found)를 뱉는 건 서버가 "살아있다"는 뜻입니다.
        // 따라서 404는 에러지만 '생존'으로 처리해줘야 안전합니다.
        if (error.response && error.response.status === 404) {
          setIsServerLive(true);
        } else {
          // 진짜 죽은 경우: 502(Bad Gateway), Network Error, Timeout
          console.warn("Backend Down:", error.message);
          setIsServerLive(false);
        }
      }
    };

    checkServerStatus();
  }, []);

  // -------------------------------------------------------

  // 1. 체크 중일 때 (아주 잠깐)
  if (isServerLive === null) {
    return null; // 흰 화면이 싫으면 <div className="loading">...</div> 등 추가
  }

  // 2. 서버가 죽었을 때 -> 점검 페이지로 덮어쓰기
  if (isServerLive === false) {
    return <MaintenancePage />;
  }

  // 3. 서버가 살아있을 때 -> 원래 앱(Routes) 보여줌
  return children;
};

export default MaintenanceGuard;
