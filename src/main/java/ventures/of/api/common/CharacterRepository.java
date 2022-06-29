package ventures.of.api.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ventures.of.api.model.db.Character;

@Repository
@RepositoryRestResource(exported = false)
public interface CharacterRepository extends JpaRepository<Character, String>, JpaSpecificationExecutor<Character> {

    long countByOnlineTrue();
}
