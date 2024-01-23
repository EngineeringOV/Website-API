package ventures.of.api.common.jpa.repositories.custom;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ventures.of.api.common.jpa.model.custom.XnoWallet;

@Repository
@RepositoryRestResource(exported = false)
public interface XnoWalletRepository extends JpaRepository<XnoWallet, String>, JpaSpecificationExecutor<XnoWallet> {

    XnoWallet findByAccountId(long id);

}
