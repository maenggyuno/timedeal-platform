import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// 로그인 및 사이트 입장 권한 관련
import { AuthProvider, useAuth } from './contexts/AuthContext';
import SellerPrivateRoute from './components/SellerPrivateRoute';
import MartAuthRoute from './components/MartAuthRoute';
import SellerLoginRedirect from './components/SellerLoginRedirect';

// 공통
import HomePage from './pages/HomePage';
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler';
import AuthCallback from './components/AuthCallback';
import MaintenanceGuard from './components/MaintenanceGuard';
// 구매자 사이트
import MainPage from './pages/buyer/MainPage';
import CaterorySearchPage from './pages/buyer/CaterorySearchPage';
import SearchPage from './pages/buyer/SearchPage';
import ProductDetailPage from './pages/buyer/ProductDetailPage';
import MapLocationPage from './pages/buyer/MapLocationPage';
import PurchasePage from './pages/buyer/PurchasePage';
import CartPage from './pages/buyer/CartPage';
import OrderCompletePage from './pages/buyer/OrderCompletePage';
import TossCheckout from './pages/buyer/TossCheckout';
import TossSuccess from './pages/buyer/TossSuccess';
import TossFail from './pages/buyer/TossFail';

// 판매자 사이트
import LoginPage from './pages/seller/LoginPage';
import MainDashboard from './pages/seller/MainDashboard';
import MartDashboard from './pages/seller/MartDashboard';
import MyMartPage from './pages/seller/MyMartPage';
import ProductCheckPage from './pages/seller/ProductCheckPage';
import ProductRegistrationPage from './pages/seller/ProductRegistrationPage';
import ReservationManagementPage from './pages/seller/ReservationManagementPage';
import Main_StoreCreatePage from './pages/seller/Main_StoreCreatePage';
import StoreManagePage from './pages/seller/StoreManagePage';
import QrScanPage from "./pages/seller/QrScanPage";

import './index.css';


function AppContent() {

    const { loading } = useAuth();

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <Router>
          <MaintenanceGuard>
            <Routes>
                {/* 공통 부분 */}
                <Route path="/" element={<Navigate to="/home" replace />} />
                <Route path="/home" element={<HomePage />} />

                {/* 로그인 */}
                <Route path="/login/success" element={<OAuth2RedirectHandler />} />
                <Route path="/login/failure" element={<OAuth2RedirectHandler />} />
                <Route path="/auth/callback" element={<AuthCallback />} />

                {/* 구매자 사이트 */}
                <Route path="/buyer" element={<MainPage />} />
                <Route path="/buyer/search/category" element={<CaterorySearchPage />} />
                <Route path="/buyer/search" element={<SearchPage />} />
                <Route path="/buyer/product/:productId" element={<ProductDetailPage />} />
                <Route path="/buyer/map" element={<MapLocationPage />} />
                <Route path="/buyer/purchase" element={<PurchasePage />} />
                <Route path="/buyer/cart" element={<CartPage />} />
                <Route path="/buyer/order-complete" element={<OrderCompletePage />} />
                <Route path="/toss-checkout" element={<TossCheckout />} />
                <Route path="/toss/success" element={<TossSuccess />} />
                <Route path="/toss/fail" element={<TossFail />} />

                {/* 판매자 사이트 */}
                <Route element={<SellerLoginRedirect />}>
                    <Route path="/seller/login" element={<LoginPage />} />
                </Route>
                <Route element={<SellerPrivateRoute />}>
                    <Route path="/seller/dashboard" element={<MainDashboard />} />
                    <Route path="/seller/store/create" element={<Main_StoreCreatePage />} />
                    <Route path="/seller/store/manage/:storeId" element={<StoreManagePage />} />

                    <Route element={<MartAuthRoute />}>
                        <Route path="/seller/mart/:martId" element={<MartDashboard />} />
                        <Route path="/seller/mart/:martId/info" element={<MyMartPage />} />
                        <Route path="/seller/mart/:martId/check" element={<ProductCheckPage />} />
                        <Route path="/seller/mart/:martId/register" element={<ProductRegistrationPage />} />
                        <Route path="/seller/mart/:martId/customers" element={<ReservationManagementPage />} />
                        <Route path="/seller/mart/:martId/qr-scan" element={<QrScanPage />} />
                    </Route>
                </Route>
            </Routes>
          </MaintenanceGuard>
        </Router>
    );
}

function App() {
    return (
        <AuthProvider>
            <AppContent />
        </AuthProvider>
    );
}

export default App;
