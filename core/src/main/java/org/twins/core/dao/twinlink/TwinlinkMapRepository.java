package org.twins.core.dao.twinlink;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.twinflow.TwinflowEntity;

import java.util.UUID;

@Repository
public interface TwinlinkMapRepository extends CrudRepository<TwinlinkMapEntity, UUID>, JpaSpecificationExecutor<TwinlinkMapEntity> {
}
