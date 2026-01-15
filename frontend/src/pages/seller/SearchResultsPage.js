import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import styles from '../../styles/seller/SearchResultsPage.module.css';
import Header from '../../components/seller/Header';
import Footer from '../../components/seller/Footer';

const SearchResultsPage = () => {
    const location = useLocation();
    const query = new URLSearchParams(location.search).get('query');
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchResults = async () => {
            try {
                const response = await axios.get(`/api/search?query=${encodeURIComponent(query)}`);
                setResults(response.data);
            } catch (error) {
                console.error('검색 중 오류 발생:', error);
                setResults([]);
            } finally {
                setLoading(false);
            }
        };

        if (query) fetchResults();
    }, [query]);

    return (
        <>
            <Header />
            <div className="search-results-page" style={{ padding: '60px', color: 'white' }}>
                <h1>검색 결과: “{query}”</h1>
                {loading ? (
                    <p>검색 중입니다...</p>
                ) : results.length > 0 ? (
                    <ul>
                        {results.map((item, index) => (
                            <li key={index}>{item.name}</li>
                        ))}
                    </ul>
                ) : (
                    <p className={styles.noResultsMessage}>검색 결과가 없습니다.</p>
                )}
            </div>
            <Footer />
        </>
    );
};

export default SearchResultsPage;
