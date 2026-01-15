package capstone.service.seller;

import capstone.controller.seller.SellerUserSearchResponse;
import capstone.dto.seller.response.SellerEmployeeListResponse;
import capstone.repository.seller.SellerEmployeeJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerEmployeeService {


    private final SellerEmployeeJdbcRepository sellerEmployeeRepository;

    @Transactional(readOnly = true)
    public List<SellerEmployeeListResponse> findEmployeesByStoreId(Long storeId) {
        return sellerEmployeeRepository.findEmployeesByStoreId(storeId);
    }

    /**
     * 총괄 관리자 권한을 위임
     */
    @Transactional
    public void delegateManager(Long storeId, Long currentManagerId, Long newManagerId) {
        sellerEmployeeRepository.delegateManagerAuthority(storeId, currentManagerId, newManagerId);
    }

    @Transactional
    public void deleteEmployees(Long storeId, List<Long> userIds) {
        sellerEmployeeRepository.deleteEmployees(storeId, userIds);
    }

    @Transactional(readOnly = true)
    public SellerUserSearchResponse searchUserByEmail(Long storeId, String email) {
        Optional<SellerUserSearchResponse> userOpt = sellerEmployeeRepository.findUserByEmail(email);

        if (userOpt.isEmpty()) {
            return new SellerUserSearchResponse(null, null, email, "NOT_FOUND");
        }

        SellerUserSearchResponse foundUser = userOpt.get();
        boolean isEmployee = sellerEmployeeRepository.isAlreadyEmployee(storeId, foundUser.getUserId());

        if (isEmployee) {
            return new SellerUserSearchResponse(foundUser.getUserId(), foundUser.getName(), foundUser.getEmail(), "ALREADY_EMPLOYEE");
        }

        return new SellerUserSearchResponse(foundUser.getUserId(), foundUser.getName(), foundUser.getEmail(), "OK");
    }

    @Transactional
    public void addEmployees(Long storeId, List<Long> userIds) {
        List<Long> newEmployeeIds = userIds.stream()
                .filter(userId -> !sellerEmployeeRepository.isAlreadyEmployee(storeId, userId))
                .toList();

        if (!newEmployeeIds.isEmpty()) {
            sellerEmployeeRepository.addEmployees(storeId, newEmployeeIds);
        }
    }
}
