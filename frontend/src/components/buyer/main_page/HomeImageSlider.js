import React, { useState, useEffect, useRef } from 'react';
import styles from '../../../styles/buyer/HomeImageSlider.module.css';

const ImageSlider = ({ images }) => {
    const [currentIndex, setCurrentIndex] = useState(0);
    const [previousIndex, setPreviousIndex] = useState(0);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const [isDesktop, setIsDesktop] = useState(false);
    const sliderRef = useRef(null);

    // 화면 크기 감지
    useEffect(() => {
        const checkIfDesktop = () => {
            setIsDesktop(window.innerWidth >= 1024);
        };

        // 초기 로드시 체크
        checkIfDesktop();

        // 창 크기 변경 시 체크
        window.addEventListener('resize', checkIfDesktop);

        // 클린업 함수
        return () => {
            window.removeEventListener('resize', checkIfDesktop);
        };
    }, []);

    // 이미지 인덱스 변경 시 애니메이션 처리
    useEffect(() => {
        if (currentIndex !== previousIndex) {
            setIsTransitioning(true);
            setPreviousIndex(currentIndex);
        }
    }, [currentIndex, previousIndex]);

    // 자동 슬라이드 (5초마다)
    useEffect(() => {
        const interval = setInterval(() => {
            if (!isTransitioning) {
                setCurrentIndex((prevIndex) => (prevIndex + 1) % images.length);
            }
        }, 5000);

        return () => clearInterval(interval);
    }, [images.length, isTransitioning]);

    // 트랜지션 종료 처리
    const handleTransitionEnd = () => {
        setIsTransitioning(false);
    };

    // 인디케이터 클릭 핸들러
    const goToSlide = (index) => {
        if (!isTransitioning && index !== currentIndex) {
            setCurrentIndex(index);
        }
    };

    return (
        <section className={styles["image-section"]}>
            <div className={styles["slider-container"]}>
                {/* 왼쪽/오른쪽 네비게이션 버튼 */}
                <div className={styles["slider-nav"]}>
                    <button
                        className={styles["nav-button"]}
                        onClick={() => goToSlide((currentIndex - 1 + images.length) % images.length)}
                        disabled={isTransitioning}
                        aria-label="이전 슬라이드"
                    >
                        {/*<span style={{ fontSize: '24px', fontWeight: 'bold' }}>&lsaquo;</span>*/}
                        <span style={{ fontSize: '35px', position: 'relative', top: '-5px', left: '-1px' }}>&lsaquo;</span>
                    </button>
                    <button
                        className={styles["nav-button"]}
                        onClick={() => goToSlide((currentIndex + 1) % images.length)}
                        disabled={isTransitioning}
                        aria-label="다음 슬라이드"
                    >
                        {/*<span style={{ fontSize: '24px', fontWeight: 'bold' }}>&rsaquo;</span>*/}
                        <span style={{ fontSize: '35px', position: 'relative', top: '-5px' }}>&rsaquo;</span>
                    </button>
                </div>

                <div
                    className={styles["slider"]}
                    style={{
                        transform: `translateX(-${currentIndex * 100}%)`,
                        transition: 'transform 0.8s cubic-bezier(0.4, 0.0, 0.2, 1)'
                    }}
                    onTransitionEnd={handleTransitionEnd}
                >
                    {images.map((image, index) => (
                        <div
                            key={index}
                            className={styles["slide"]}
                        >
                            <img
                                src={image}
                                alt={`메인 이미지 ${index + 1}`}
                                className={styles["slider-image"]}
                            />
                        </div>
                    ))}
                </div>

                {/* 인디케이터 */}
                <div className={styles["slider-indicators"]}>
                    {images.map((_, index) => (
                        <button
                            key={index}
                            className={`${styles["indicator"]} ${
                                index === currentIndex ? styles["active"] : ""
                            }`}
                            onClick={() => goToSlide(index)}
                            aria-label={`슬라이드 ${index + 1}`}
                            disabled={isTransitioning}
                        />
                    ))}
                </div>

            </div>
        </section>
    );
};

export default ImageSlider;