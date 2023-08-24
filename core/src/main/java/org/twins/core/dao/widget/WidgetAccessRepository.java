package org.twins.core.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WidgetAccessRepository extends CrudRepository<WidgetAccessEntity, UUID>, JpaSpecificationExecutor<WidgetAccessEntity> {
}
