package capstone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "user_id")
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowId followId = (FollowId) o;
        return Objects.equals(storeId, followId.storeId) && Objects.equals(userId, followId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, userId);
    }
}
