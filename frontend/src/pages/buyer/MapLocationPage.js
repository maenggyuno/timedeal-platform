import React from 'react';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import LocationMap from '../../components/buyer/map_location_page/LocationMap';
import mapLocationPageStyles from '../../styles/buyer/MapLocationPage.module.css';

const MapLocationPage = () => {
    return (
        <div className={mapLocationPageStyles["map-location-page"]}>
            <Header />
            <main className={mapLocationPageStyles["map-content"]}>
                <h1 className={mapLocationPageStyles["page-title"]}>동네 매장 보기</h1>
                <LocationMap />
            </main>
            <Footer />
        </div>
    );
};

export default MapLocationPage;