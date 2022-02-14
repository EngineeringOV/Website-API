package io.github.engineeringov.website.api.common.jpa.repositories.acore;

import io.github.engineeringov.website.api.common.jpa.model.acore.GameMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface GameMailRepository extends JpaRepository<GameMail, Integer>, JpaSpecificationExecutor<GameMail> {
}
