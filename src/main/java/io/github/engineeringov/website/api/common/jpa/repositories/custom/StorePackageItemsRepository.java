package io.github.engineeringov.website.api.common.jpa.repositories.custom;


import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface StorePackageItemsRepository extends JpaRepository<StorePackageItem, String>, JpaSpecificationExecutor<StorePackageItem> {
}
