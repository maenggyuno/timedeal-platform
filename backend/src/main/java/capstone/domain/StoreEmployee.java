package capstone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "storeEmployee")
public class StoreEmployee {

    @EmbeddedId
    private StoreEmployeeId id; // 복합 키를 ID로 사용

    @Column(nullable = false)
    private Integer authority; // 0: 직원, 1: 오너
}
