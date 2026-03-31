package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TwinClassFreezeRepository extends CrudRepository<TwinClassFreezeEntity, UUID>, JpaSpecificationExecutor<TwinClassFreezeEntity> {
    List<TwinClassFreezeEntity> findAllByIdIn(Collection<UUID> ids);
}
