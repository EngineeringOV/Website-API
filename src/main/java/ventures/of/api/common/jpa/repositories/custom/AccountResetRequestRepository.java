package ventures.of.api.common.jpa.repositories.custom;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ventures.of.api.common.jpa.model.custom.AccountReset;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Repository
@RepositoryRestResource(exported = false)
public interface AccountResetRequestRepository extends JpaRepository<AccountReset, String>, JpaSpecificationExecutor<AccountReset> {
    ArrayList<AccountReset> findByUuidAndEmailAndValidRequestIsTrue(String uuid, String email);
    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime createdAt);
}
