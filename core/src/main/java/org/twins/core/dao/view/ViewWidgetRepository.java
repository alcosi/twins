package org.twins.core.dao.view;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViewWidgetRepository extends CrudRepository<ViewWidgetEntity, UUID>, JpaSpecificationExecutor<ViewWidgetEntity> {
}
