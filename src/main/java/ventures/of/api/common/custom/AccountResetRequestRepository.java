package ventures.of.api.common.custom;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ventures.of.api.model.db.custom.AccountResetRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Repository
@RepositoryRestResource(exported = false)
public interface AccountResetRequestRepository extends JpaRepository<AccountResetRequest, String>, JpaSpecificationExecutor<AccountResetRequest> {

    ArrayList<AccountResetRequest> findByUuidAndEmailAndValidRequestIsTrue(String uuid, String email);
    int countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime createdAt);


}
