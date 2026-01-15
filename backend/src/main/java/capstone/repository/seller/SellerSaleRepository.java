package capstone.repository.seller;

import capstone.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerSaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByProduct_StoreIdAndStatus(Long storeId, Integer status);
}
