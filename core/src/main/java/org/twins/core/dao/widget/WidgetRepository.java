package org.twins.core.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.view.ViewEntity;

import java.util.UUID;

@Repository
public interface WidgetRepository extends CrudRepository<WidgetEntity, UUID>, JpaSpecificationExecutor<WidgetAccessEntity> {
}
