package capstone.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_id")
    private Long qrId;

    @Column(nullable = false, unique = true, length = 100)
    private String uuid;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(columnDefinition = "TINYINT DEFAULT 2")
    private Byte count;
}
