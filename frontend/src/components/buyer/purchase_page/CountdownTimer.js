import React, { useState, useEffect } from 'react';

const CountdownTimer = ({ expiryTimestamp }) => {
  const calculateTimeLeft = () => {
    
    let utcExpiryTimestamp = expiryTimestamp;
    if (expiryTimestamp && !expiryTimestamp.endsWith('Z')) {
        utcExpiryTimestamp = expiryTimestamp + 'Z';
    }

    const difference = new Date(utcExpiryTimestamp) - new Date();
    let timeLeft = {};

    if (difference > 0) {
      timeLeft = {
        hours: Math.floor(difference / (1000 * 60 * 60)),
        minutes: Math.floor((difference / 1000 / 60) % 60),
        seconds: Math.floor((difference / 1000) % 60),
      };
    }
    return timeLeft;
  };

  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1000);
    return () => clearInterval(timer);
  }, [expiryTimestamp]);

  // 남은 시간이 없으면 '시간 만료' 텍스트 출력
  if (!timeLeft.hours && !timeLeft.minutes && !timeLeft.seconds) {
    return <span>시간 만료</span>;
  }
  
  // 두 자리 수로 포맷팅 (예: 7초 -> 07초)
  const format = (num) => String(num).padStart(2, '0');

  return (
    <span>
      {format(timeLeft.hours)}:{format(timeLeft.minutes)}:{format(timeLeft.seconds)}
    </span>
  );
};

export default CountdownTimer;