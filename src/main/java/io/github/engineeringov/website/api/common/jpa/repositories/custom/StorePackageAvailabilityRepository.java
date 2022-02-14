package io.github.engineeringov.website.api.common.jpa.repositories.custom;


import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Repository
@RepositoryRestResource(exported = false)
public interface StorePackageAvailabilityRepository extends JpaRepository<StorePackageAvailability, String>, JpaSpecificationExecutor<StorePackageAvailability> {
    //acore_custom.store_package_availability
    @Query("SELECT e FROM StorePackageAvailability e WHERE :date BETWEEN e.startsAt AND e.endsAt")
    ArrayList<StorePackageAvailability> findByAvailableAtDate(LocalDateTime date);

}
