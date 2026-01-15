// src/main/java/capstone/repository/StoreRepository.java
package capstone.repository.seller;

import capstone.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerStoreCreateRepository extends JpaRepository<Store, Long> {

}
