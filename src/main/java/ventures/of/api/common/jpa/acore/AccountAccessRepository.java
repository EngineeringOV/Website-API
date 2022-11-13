package ventures.of.api.common.jpa.acore;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ventures.of.api.model.db.acore.AccountAccess;

@Repository
@RepositoryRestResource(exported = false)
public interface AccountAccessRepository extends JpaRepository<AccountAccess, String>, JpaSpecificationExecutor<AccountAccess> {
}
