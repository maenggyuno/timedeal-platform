package capstone.repository.buyer;

import capstone.domain.Store;
import capstone.dto.buyer.response.BuyerStoreLocationResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

@Repository
public class BuyerMapRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuyerMapRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 모든 상점 검색 (사용하지는 않음)
    public List<Store> findAll() {
        String sql = "SELECT store_id, name, address, latitude, longitude, phone_number, owner_id, business_number, opening_time, closing_time FROM stores";
        return jdbcTemplate.query(sql, new RowMapper<Store>() {
            @Override
            public Store mapRow(ResultSet rs, int rowNum) throws SQLException {
                Store store = new Store();
                store.setStoreId(rs.getLong("store_id"));
                store.setName(rs.getString("name"));
                store.setAddress(rs.getString("address"));
                store.setLatitude(rs.getBigDecimal("latitude"));
                store.setLongitude(rs.getBigDecimal("longitude"));
                store.setPhoneNumber(rs.getString("phone_number"));
                store.setOwnerId(rs.getLong("owner_id"));
                store.setBusinessNumber(rs.getString("business_number"));

                // DB의 TIME 타입을 LocalTime으로 변환 (null 체크 포함)
                Time openingTime = rs.getTime("opening_time");
                if (openingTime != null) {
                    store.setOpeningTime(openingTime.toLocalTime());
                }
                Time closingTime = rs.getTime("closing_time");
                if (closingTime != null) {
                    store.setClosingTime(closingTime.toLocalTime());
                }

                return store;
            }
        });
    }

    // 근처 상점 검색
    public List<Store> findNearbyStores(double latitude, double longitude, double distance) {
        // 하버사인 공식을 사용한 거리 계산 SQL
        String sql = "SELECT * FROM stores " +
                "WHERE (6371 * acos(cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) + sin(radians(?)) * sin(radians(latitude)))) <= ? " +
                "AND is_deleted = false";

        return jdbcTemplate.query(sql, new RowMapper<Store>() {

            @Override
            public Store mapRow(ResultSet rs, int rowNum) throws SQLException {
                Store store = new Store();
                store.setStoreId(rs.getLong("store_id"));
                store.setName(rs.getString("name"));
                store.setAddress(rs.getString("address"));
                store.setLatitude(rs.getBigDecimal("latitude"));
                store.setLongitude(rs.getBigDecimal("longitude"));
                store.setPhoneNumber(rs.getString("phone_number"));
                store.setOwnerId(rs.getLong("owner_id"));
                store.setBusinessNumber(rs.getString("business_number"));

                // DB의 TIME 타입을 LocalTime으로 변환 (null 체크 포함)
                Time openingTime = rs.getTime("opening_time");
                if (openingTime != null) {
                    store.setOpeningTime(openingTime.toLocalTime());
                }
                Time closingTime = rs.getTime("closing_time");
                if (closingTime != null) {
                    store.setClosingTime(closingTime.toLocalTime());
                }

                return store;
            }
        }, latitude, longitude, latitude, distance);
    }

    public BuyerStoreLocationResponse getStoreLocation(Long storeId) {
        String sql = "SELECT latitude, longitude, name, address, phone_number, opening_time, closing_time " +
                "FROM stores " +
                "WHERE store_id = ?";

        return jdbcTemplate.queryForObject(sql, new RowMapper<BuyerStoreLocationResponse>() {
            @Override
            public BuyerStoreLocationResponse mapRow(ResultSet rs, int rowNum) throws SQLException {

                BuyerStoreLocationResponse response = new BuyerStoreLocationResponse(
                        rs.getBigDecimal("latitude"),
                        rs.getBigDecimal("longitude"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone_number"),
                        rs.getTime("opening_time"),
                        rs.getTime("closing_time"));

                return response;
            }
        }, storeId);
    }
}
