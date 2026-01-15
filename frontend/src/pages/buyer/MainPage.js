import React from 'react';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import HomeProductGrid from '../../components/buyer/main_page/HomeProductGrid';
import ImageSlider from '../../components/buyer/main_page/HomeImageSlider';

const MainPage = () => {

    // 이미지 배열 - ImageSlider 컴포넌트에 전달
    const images = [
        '/buyer/buyer-home-image1.png',
        '/buyer/buyer-home-image2.png',
        '/buyer/buyer-home-image3.png',
    ];

    return (
        <>
            <Header />
            <main>
                <ImageSlider images={images} />
                <HomeProductGrid />
            </main>
            <Footer />
        </>
    );
};

export default MainPage;