import React from 'react';
import { useLocation } from 'react-router-dom';
import Header from '../../components/buyer/Header';
import Footer from '../../components/buyer/Footer';
import CategoryProductGrid from '../../components/buyer/CategoryProductGrid';

const CaterorySearchPage = () => {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const category = searchParams.get('category'); // URL에서 ?category=과일 같은 값을 추출

  return (
    <>
      <Header />
      <main>
        <CategoryProductGrid category={category} />
      </main>
      <Footer />
    </>
  );
};

export default CaterorySearchPage;