package capstone.repository.buyer;

import capstone.domain.Product;
import capstone.dto.buyer.response.BuyerProductDetailResponse;
import capstone.dto.buyer.response.BuyerProductDetailStoreResponse;
import capstone.dto.buyer.response.BuyerProductSearchResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class BuyerProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuyerProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 메인화면 (상품목록출력)
    public List<BuyerProductSearchResponse> getProductsNearby(double latitude, double longitude, double distance, int page, int size) {
        // 하버사인 공식 사용
        String sql = "SELECT p.product_id, p.product_name, p.price, p.product_img_src, p.category " +
                "FROM products p " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE (6371 * acos(cos(radians(?)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(?)) + sin(radians(?)) * sin(radians(s.latitude)))) <= ? " +
                "AND s.is_deleted = false " +
                "AND (p.status = 0 OR p.status = 1) " +
                "AND p.quantity > 0 " +
                "AND ( " +
                "    (s.opening_time < s.closing_time AND TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) BETWEEN s.opening_time AND s.closing_time) " +
                "    OR " +
                "    (s.opening_time > s.closing_time AND (TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) >= s.opening_time OR TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) < s.closing_time)) " +
                ") " +
                "ORDER BY p.product_id DESC " +
                "LIMIT ? OFFSET ?";

        int offset = page * size;
        return jdbcTemplate.query(sql, new RowMapper<BuyerProductSearchResponse>() {
            @Override
            public BuyerProductSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BuyerProductSearchResponse(rs.getLong("product_id"), rs.getString("product_img_src"),
                        rs.getString("product_name"), rs.getLong(("price")), rs.getString("category"));
            }
        }, latitude, longitude, latitude, distance, size, offset);
    }

    // 선택한 상품정보확인
    public BuyerProductDetailResponse getProductById(Long userId, Long productId) {

        if (userId != null) {   // 사용자가 로그인을 했을 경우
            String sql = "SELECT " +
                    "    p.product_id, p.product_img_src, p.product_name, p.price, p.expiration_date, p.description, p.category, p.quantity, " +
                    "    s.store_id, s.name AS store_name, s.address, s.payment_method, " +
                    "    COUNT(DISTINCT f.user_id) AS follower_count, " +
                    "    COUNT(DISTINCT r.review_id) AS review_count, " +
                    "    AVG(r.rating) AS average_rating, " +
                    "    CASE WHEN f_user.user_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_following " +
                    "FROM products p " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN follows f ON s.store_id = f.store_id " +
                    "LEFT JOIN follows f_user ON s.store_id = f_user.store_id AND f_user.user_id = ? " +
                    "LEFT JOIN order_items oi ON s.store_id = oi.seller_id " +
                    "LEFT JOIN reviews r ON oi.order_id = r.order_id " +
                    "WHERE p.product_id = ? " +
                    "GROUP BY s.store_id, s.name, s.address, p.product_id, f_user.user_id";

            return jdbcTemplate.queryForObject(sql, new RowMapper<BuyerProductDetailResponse>() {
                @Override
                public BuyerProductDetailResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                    BuyerProductDetailStoreResponse store = new BuyerProductDetailStoreResponse(
                            rs.getLong("store_id"),
                            rs.getString("store_name"),
                            rs.getString("address"),
                            rs.getInt("payment_method"),
                            rs.getDouble("average_rating"),
                            rs.getLong("follower_count"),
                            rs.getLong("review_count"),
                            rs.getBoolean("is_following")
                    );

                    return new BuyerProductDetailResponse(
                            rs.getLong("product_id"),
                            rs.getString("product_img_src"),
                            rs.getString("product_name"),
                            rs.getLong("price"),
                            rs.getInt("quantity"),
                            rs.getTimestamp("expiration_date").toLocalDateTime(),
                            rs.getString("description"),
                            store,
                            rs.getString("category")
                    );
                }
            }, userId, productId);
        }
        else {  // 사용자가 로그인을 하지 않았을 경우
            String sql = "SELECT " +
                    "    p.product_id, p.product_img_src, p.product_name, p.price, p.expiration_date, p.description, p.category, p.quantity," +
                    "    s.store_id, s.name AS store_name, s.address, s.payment_method, " +
                    "    COUNT(DISTINCT f.user_id) AS follower_count, " +
                    "    COUNT(DISTINCT r.review_id) AS review_count, " +
                    "    AVG(r.rating) AS average_rating, " +
                    "    FALSE AS is_following " +
                    "FROM products p " +
                    "JOIN stores s ON p.store_id = s.store_id " +
                    "LEFT JOIN follows f ON s.store_id = f.store_id " +
                    "LEFT JOIN order_items oi ON s.store_id = oi.seller_id " +
                    "LEFT JOIN reviews r ON oi.order_id = r.order_id " +
                    "WHERE p.product_id = ? " +
                    "GROUP BY s.store_id, s.name, s.address, p.product_id";

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                BuyerProductDetailStoreResponse store = new BuyerProductDetailStoreResponse(
                        rs.getLong("store_id"),
                        rs.getString("store_name"),
                        rs.getString("address"),
                        rs.getInt("payment_method"),
                        rs.getDouble("average_rating"),
                        rs.getLong("follower_count"),
                        rs.getLong("review_count"),
                        rs.getBoolean("is_following")
                );

                return new BuyerProductDetailResponse(
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getLong("price"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("expiration_date").toLocalDateTime(),
                        rs.getString("description"),
                        store,
                        rs.getString("category")
                );
            }, productId);
        }
    }

    // 카테고리 기능
    public List<BuyerProductSearchResponse> getNearbyProductsByCategory(double latitude, double longitude, double distance, String category, int page, int size) {
        // 하버사인 공식 사용
        String sql = "SELECT p.product_id, p.product_name, p.price, p.product_img_src, p.category " +
                "FROM products p " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE (6371 * acos(cos(radians(?)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(?)) + sin(radians(?)) * sin(radians(s.latitude)))) <= ? " +
                "AND s.is_deleted = false " +
                "AND (p.status = 0 OR p.status = 1) " +
                "AND p.category = ? " +
                "AND p.quantity > 0 " +
                "AND ( " +
                "    (s.opening_time < s.closing_time AND TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) BETWEEN s.opening_time AND s.closing_time) " +
                "    OR " +
                "    (s.opening_time > s.closing_time AND (TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) >= s.opening_time OR TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) < s.closing_time)) " +
                ") " +
                "ORDER BY p.product_id DESC " +
                "LIMIT ? OFFSET ?";

        int offset = page * size;
        return jdbcTemplate.query(sql, new RowMapper<BuyerProductSearchResponse>() {
            @Override
            public BuyerProductSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BuyerProductSearchResponse(
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getLong(("price")),
                        rs.getString("category"));
            }
        }, latitude, longitude, latitude, distance, category, size, offset);
    }

    // 검색기능
    public List<BuyerProductSearchResponse> getNearbyProductsBySearchString(double latitude, double longitude, double distance, String searchString, int page, int size) {
        // 하버사인 공식 사용
        String sql = "SELECT p.product_id, p.product_name, p.price, p.product_img_src, p.category " +
                "FROM products p " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE (6371 * acos(cos(radians(?)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(?)) + sin(radians(?)) * sin(radians(s.latitude)))) <= ? " +
                "AND s.is_deleted = false " +
                "AND (p.status = 0 OR p.status = 1) " +
                "AND p.product_name LIKE ? " +
                "AND p.quantity > 0 " +
                "AND ( " +
                "    (s.opening_time < s.closing_time AND TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) BETWEEN s.opening_time AND s.closing_time) " +
                "    OR " +
                "    (s.opening_time > s.closing_time AND (TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) >= s.opening_time OR TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) < s.closing_time)) " +
                ") " +
                "ORDER BY p.product_id DESC " +
                "LIMIT ? OFFSET ?";

        String wildcardSearchString = "%" + searchString + "%";
        int offset = page * size;

        return jdbcTemplate.query(sql, new RowMapper<BuyerProductSearchResponse>() {
            @Override
            public BuyerProductSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BuyerProductSearchResponse(
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getLong("price"),
                        rs.getString("category"));
            }
        }, latitude, longitude, latitude, distance, wildcardSearchString, size, offset);
    }

    // 지도 모달에서 상점 상품 띄우는 거
    public List<BuyerProductSearchResponse> getProductsByStore(Long storeId, int page, int size) {
        String sql = "SELECT p.product_id, p.product_name, p.price, p.product_img_src, p.category, s.is_deleted " +
                "FROM products p " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE p.store_id = ? " +
                "AND s.is_deleted = false " +
                "AND (p.status = 0 OR p.status = 1) " +
                "AND p.quantity > 0 " +
                "AND ( " +
                "    (s.opening_time < s.closing_time AND TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) BETWEEN s.opening_time AND s.closing_time) " +
                "    OR " +
                "    (s.opening_time > s.closing_time AND (TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) >= s.opening_time OR TIME(CONVERT_TZ(NOW(), 'UTC', 'Asia/Seoul')) < s.closing_time)) " +
                ") " +
                "ORDER BY p.product_id DESC " +
                "LIMIT ? OFFSET ?";

        int offset = page * size;
        return jdbcTemplate.query(sql, new RowMapper<BuyerProductSearchResponse>() {
            @Override
            public BuyerProductSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BuyerProductSearchResponse(rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getLong(("price")),
                        rs.getString("category"),
                        rs.getBoolean("is_deleted"));
            }
        }, storeId, size, offset);
    }

    public Optional<Product> findById(Long productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Product.class), productId);
            return Optional.of(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
