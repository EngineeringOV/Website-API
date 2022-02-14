package io.github.engineeringov.website.api.common.jpa.repositories.acore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import io.github.engineeringov.website.api.common.jpa.model.acore.ItemInstance;

@Repository
@RepositoryRestResource(exported = false)
public interface ItemInstanceRepository extends JpaRepository<ItemInstance, Short>, JpaSpecificationExecutor<ItemInstance> {
}
