package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryRepository extends CrudRepository<TwinFactoryEntity, UUID>, JpaSpecificationExecutor<TwinFactoryEntity> {
}
