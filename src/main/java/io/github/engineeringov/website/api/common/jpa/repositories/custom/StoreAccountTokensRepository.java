package io.github.engineeringov.website.api.common.jpa.repositories.custom;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import io.github.engineeringov.website.api.common.jpa.model.custom.StoreAccountTokens;

@Repository
@RepositoryRestResource(exported = false)
public interface StoreAccountTokensRepository extends JpaRepository<StoreAccountTokens, String>, JpaSpecificationExecutor<StoreAccountTokens> {
}
