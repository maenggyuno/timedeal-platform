import {React, useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import axiosConfig from '../../services/axiosConfig';
import { useAuth } from '../../contexts/AuthContext';
import LoginModal from './LoginModal';
import { BiCategory } from "react-icons/bi";
import { BsCart4 } from "react-icons/bs";
import { FaRegMap, FaSearch, FaRegBell, FaRegUser, FaGoogle } from "react-icons/fa";
import { SiNaver } from "react-icons/si";
import headerStyles from '../../styles/buyer/Header.module.css';

const IconButton = ({ icon, label, onClick, className, ariaLabel }) => {
    return (
        <div className={headerStyles["icon-button-container"]}>
            <button
                className={className}
                aria-label={ariaLabel || label}
                onClick={onClick}
            >
                {icon}
            </button>
            <div className={headerStyles["icon-label"]}>
                {label}
            </div>
        </div>
    );
};

const DropdownMenu = ({ icon, label, menuItems, className, ariaLabel, menuClassName }) => {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);
    const location = useLocation();

    const handleClick = () => {
        setIsOpen(!isOpen);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [dropdownRef]);

    useEffect(() => {
        if (isOpen) {
            setIsOpen(false);
        }
    }, [location]);

    return (
        <div
            className={headerStyles["dropdown-container"]}
            ref={dropdownRef}
        >
            <div className={headerStyles["icon-button-container"]} onClick={handleClick}>
                <button
                    className={className}
                    aria-label={ariaLabel || label}
                    type="button"
                >
                    {icon}
                </button>
                <div className={headerStyles["icon-label"]}>
                    {label}
                </div>
            </div>
            {isOpen && (
                <div className={menuClassName || headerStyles["dropdown-menu"]}>
                    {menuItems}
                </div>
            )}
        </div>
    );
};

const CategoryMenu = () => {
    const [openCategory, setOpenCategory] = useState(null);

    const categories = {
        '신선식품': ['과일', '채소', '육류', '수산물', '유제품'],
        '가공식품': ['통조림/병조림', '즉석식품', '소스/양념', '간편조리식'],
        '건강/특수식품': ['유기농/친환경', '비건/채식', '다이어트/저칼로리'],
        '베이커리/디저트': ['빵', '케이크/파이', '쿠키/스낵'],
        '음료': ['생수/탄산수', '주스/스무디', '커피/차', '기능성 음료']
    };

    const handleCategoryClick = (categoryName) => {
        setOpenCategory(openCategory === categoryName ? null : categoryName);
    };

    return (
        <div className={headerStyles["category-accordion"]}>
            {Object.entries(categories).map(([category, subcategories]) => (
                <div key={category} className={headerStyles["category-item"]}>
                    <h3
                        className={headerStyles["category-title"]}
                        onClick={() => handleCategoryClick(category)}
                    >
                        {category}
                        <span className={`${headerStyles.arrow} ${openCategory === category ? headerStyles.open : ''}`}></span>
                    </h3>
                    {openCategory === category && (
                        <ul className={headerStyles["subcategory-list"]}>
                            {subcategories.map(sub => (
                                <li key={sub}>
                                    <Link to={`/buyer/search/category?category=${sub}`} className={headerStyles.subcategoryLink}>
                                        {sub}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            ))}
        </div>
    );
};

const Header = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isAlarmOpen, setIsAlarmOpen] = useState(false);
    const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

    // const { isLoggedIn, setUser } = useAuth();
    const { isLoggedIn, user, setUser } = useAuth();

    const handleSearch = (e) => {
        e.preventDefault();
        const query = e.target.query.value;
        navigate(`/buyer/search?query=${query}`);
    };

    const handleCartClick = () => navigate('/buyer/cart');
    const handleMapClick = () => navigate('/buyer/map');
    const handleAlarmClick = () => setIsAlarmOpen(true);

    const handleLoginClick = () => {
        localStorage.setItem('redirectPathAfterLogin', location.pathname);
        setIsLoginModalOpen(true);
    };

    const handleLogout = async () => {
        try {
            await axiosConfig.post('/api/auth/logout');
            console.log("Logout request sent to server.");
        } catch (error) {
            console.error('Logout API call failed, but proceeding with client-side logout:', error);
        } finally {
            setUser(null);
            alert('로그아웃 되었습니다.');
            navigate('/buyer/');
        }
    };

    const categoryMenuItems = <CategoryMenu />;

    // const userMenuItems = isLoggedIn ? (
    //     <>
    //         <button onClick={handleLogout} className={headerStyles.dropdownLink}>로그아웃</button>
    //         <Link to="/buyer/purchase" className={headerStyles.dropdownLink}>구매상품</Link>
    //     </>
    // ) : (
    //     <>
    //         <button onClick={handleLoginClick} className={headerStyles.dropdownLink}>로그인</button>
    //     </>
    // );
    const userMenuItems = (isLoggedIn && user) ? (
        <>
            <div className={headerStyles.userInfo}>
                <div className={headerStyles.userInfoTop}>
                    <span className={headerStyles.userName}>{user.name}</span>
                    {user.social_login_platform === 'google' &&
                        <FaGoogle className={headerStyles.platformIcon} title="Google 로그인" />
                    }
                    {user.social_login_platform === 'naver' &&
                        <SiNaver className={`${headerStyles.platformIcon} ${headerStyles.naverIcon}`} title="Naver 로그인" />
                    }
                </div>
                <span className={headerStyles.userEmail}>{user.email}</span>
            </div>
            <button onClick={handleLogout} className={headerStyles.dropdownLink}>로그아웃</button>
            <Link to="/buyer/purchase" className={headerStyles.dropdownLink}>구매상품</Link>
        </>
    ) : (
        <>
            <button onClick={handleLoginClick} className={headerStyles.dropdownLink}>로그인</button>
        </>
    );

    return (
        <header>
            <div className={headerStyles["header-content-wrapper"]}>
                <div className={headerStyles["header-main"]}>
                    <div className={headerStyles["left-section"]}>
                        <h1 className={headerStyles["shop-name"]}>
                            <Link to="/buyer/">
                                <img src="/buyer/shop_logo.png" alt="쇼핑몰 이름" className={headerStyles["shop-logo"]}/>
                            </Link>
                        </h1>
                    </div>
                    <div className={headerStyles.right}>
                        <form onSubmit={handleSearch} className={headerStyles["search-form"]} autoComplete="off">
                            <input type="text" name="query" className={headerStyles["search-box"]} placeholder="검색어를 입력해주세요" autoComplete="off" />
                            <button type="submit" className={headerStyles["search-button"]}><FaSearch /></button>
                        </form>
                    </div>
                </div>
                <nav className={headerStyles["header-nav"]}>
                    <div className={headerStyles["nav-top"]}>
                        <div className={headerStyles["category-menu"]}>
                            <DropdownMenu icon={<BiCategory size={23} />} label="카테고리" menuItems={categoryMenuItems} className={`${headerStyles["category-btn"]} ${headerStyles["icon-btn"]}`} ariaLabel="카테고리" menuClassName={headerStyles["category-dropdown"]} />
                        </div>
                        <IconButton icon={<FaRegMap size={23} />} label="지도보기" onClick={handleMapClick} className={`${headerStyles["map-view"]} ${headerStyles["icon-btn"]}`} ariaLabel="지도보기" />
                    </div>
                    <div className={headerStyles["nav-bottom"]}>
                        <IconButton icon={<BsCart4 size={26} />} label="장바구니" onClick={handleCartClick} className={`${headerStyles["cart-btn"]} ${headerStyles["icon-btn"]}`} ariaLabel="장바구니" />
                        <div className={headerStyles["user-menu"]}>
                            <DropdownMenu icon={<FaRegUser size={20} />} label="사용자" menuItems={userMenuItems} className={`${headerStyles["user-btn"]} ${headerStyles["icon-btn"]}`} ariaLabel="사용자" menuClassName={headerStyles["user-dropdown"]} />
                        </div>
                    </div>
                </nav>
            </div>
            <hr className={headerStyles["header-hr"]}></hr>

            {isLoginModalOpen && <LoginModal onClose={() => setIsLoginModalOpen(false)} />}
        </header>
    );
};

export default Header;
