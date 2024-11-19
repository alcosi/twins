package org.twins.core.dao.eraseflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EraseflowLinkCascadeRepository extends CrudRepository<EraseflowLinkCascadeEntity, UUID>, JpaSpecificationExecutor<EraseflowLinkCascadeEntity> {
    List<EraseflowLinkCascadeEntity> findByEraseflowId(UUID eraseflowId);
}
