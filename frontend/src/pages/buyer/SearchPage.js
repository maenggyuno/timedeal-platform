import React from 'react';
import { useLocation } from 'react-router-dom';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import SearchProductGrid from '../../components/buyer/SearchProductGrid';

const SearchPage = () => {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const query = searchParams.get('query');

  return (
    <>
      <Header />
      <main>
        <SearchProductGrid query={query} title="상품 검색 결과" />
      </main>
      <Footer />
    </>
  );
};

export default SearchPage;