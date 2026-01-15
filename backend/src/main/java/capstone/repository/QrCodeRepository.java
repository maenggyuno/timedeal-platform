package capstone.repository;

import capstone.domain.QrCode;
import capstone.dto.qrcode.QrCodeProductResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class QrCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public QrCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<QrCode> QR_ROW_MAPPER = new RowMapper<>() {
        @Override
        public QrCode mapRow(ResultSet rs, int rowNum) throws SQLException {
            QrCode qr = new QrCode();
            qr.setQrId(rs.getLong("qr_id"));
            qr.setUuid(rs.getString("uuid"));
            qr.setValidUntil(rs.getTimestamp("valid_until").toLocalDateTime());
            qr.setOrderId(rs.getLong("order_id"));
            try {
                qr.setCount(rs.getByte("count"));
            } catch (SQLException ignore) { /* no-op */ }
            return qr;
        }
    };

    /**
     * 기본 QR 생성: 현재 시간 기준 +1시간
     */
    public void basicCreateQrCode(String uuid, Long orderId) {
        String sql =
                "INSERT INTO qrCodes (uuid, order_id, valid_until) " +
                        "VALUES (?, ?, CONVERT_TZ(DATE_ADD(NOW(), INTERVAL 1 HOUR), 'UTC', 'Asia/Seoul'))";
        jdbcTemplate.update(sql, uuid, orderId);
    }

    /**
     * 예약 QR 생성: 상점 마감 1시간 전까지
     * - stores.closing_time 기준
     */
    public void createReservedQrCode(String uuid, Long orderId) {
        String sql =
                "INSERT INTO qrCodes (uuid, order_id, valid_until) " +
                        "SELECT ?, ?, CONVERT_TZ(DATE_SUB(TIMESTAMP(CURDATE(), s.closing_time), INTERVAL 1 HOUR), 'UTC', 'Asia/Seoul') " +
                        "FROM stores s " +
                        "JOIN orderItems oi ON s.store_id = oi.seller_id " +
                        "WHERE oi.order_id = ?";
        jdbcTemplate.update(sql, uuid, orderId, orderId);
    }

    /**
     * orderId + 현재시간(after)로 유효 QR 1건 조회 (카운트다운 표시용 등)
     */
    public Optional<QrCode> findByOrderIdAndValidUntilAfter(Long orderId, LocalDateTime currentTime) {
        String sql =
                "SELECT qr_id, uuid, valid_until, order_id " +
                        "FROM qrCodes WHERE order_id = ? AND valid_until > ?";
        try {
            QrCode qrCode = jdbcTemplate.queryForObject(sql, QR_ROW_MAPPER, orderId, currentTime);
            return Optional.ofNullable(qrCode);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * orderId + uuid로 조회 (유효시간 검사는 하지 않음)
     */
    public Optional<QrCode> findByOrderIdAndUuid(Long orderId, String uuid) {
        String sql =
                "SELECT qr_id, uuid, valid_until, order_id " +
                        "FROM qrCodes WHERE order_id = ? AND uuid = ?";
        try {
            QrCode qr = jdbcTemplate.queryForObject(sql, QR_ROW_MAPPER, orderId, uuid);
            return Optional.ofNullable(qr);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * orderId + uuid + valid_until > now(유효시간 내)로 조회
     * - QR 스캔 체크인 검증용
     */
    public Optional<QrCode> findValidByOrderIdAndUuid(Long orderId, String uuid, LocalDateTime now) {
        String sql =
                "SELECT qr_id, uuid, valid_until, order_id " +
                        "FROM qrCodes WHERE order_id = ? AND uuid = ? AND valid_until > ?";
        try {
            QrCode qr = jdbcTemplate.queryForObject(sql, QR_ROW_MAPPER, orderId, uuid, now);
            return Optional.ofNullable(qr);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 해당 주문의 QR 레코드 제거
     * - 체크인 완료 or 예약 취소 시 호출
     */
    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM qrCodes WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }

    /**
     * 유효시간 지난 QR 일괄 삭제(청소용)
     * @return 삭제된 row 수
     */
    public int deleteByValidUntilBefore(LocalDateTime time) {
        String sql = "DELETE FROM qrCodes WHERE valid_until <= ?";
        return jdbcTemplate.update(sql, time);
    }

    /**
     * (옵션) 화면용: orderId 로 valid_until 하나만 가져오기
     */
    public Optional<LocalDateTime> findValidUntilByOrderId(Long orderId) {
        String sql = "SELECT valid_until FROM qrCodes WHERE order_id = ? ORDER BY qr_id DESC LIMIT 1";
        try {
            LocalDateTime vt = jdbcTemplate.queryForObject(
                    sql,
                    (rs, i) -> rs.getTimestamp("valid_until").toLocalDateTime(),
                    orderId
            );
            return Optional.ofNullable(vt);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // 최신(valid_until가 가장 큰) QR 1건만 조회
    public Optional<QrCode> findLatestByOrderId(Long orderId) {
        String sql =
                "SELECT qr_id, uuid, valid_until, order_id, /* count 컬럼이 없다면 이 부분 제거 */ count " +
                        "FROM qrCodes WHERE order_id = ? ORDER BY valid_until DESC LIMIT 1";
        try {
            QrCode qr = jdbcTemplate.queryForObject(sql, QR_ROW_MAPPER, orderId);
            return Optional.ofNullable(qr);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     *  QR코드 상품조회
     * */
    public QrCodeProductResponse findProductFromQrCode(Long orderId) {
        String sql = "SELECT " +
                "    oi.order_id AS orderId, " +
                "    p.product_name AS productName, " +
                "    oi.quantity AS quantity, " +
                "    oi.total_price AS totalPrice, " +
                "    oi.status AS status " +
                "FROM " +
                "    orderItems oi " +
                "JOIN " +
                "    products p ON oi.product_id = p.product_id " +
                "WHERE " +
                "    oi.order_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(QrCodeProductResponse.class), orderId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}


