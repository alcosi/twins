package org.twins.core.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WidgetRepository extends CrudRepository<WidgetEntity, UUID>, JpaSpecificationExecutor<WidgetEntity> {
    @Override
    List<WidgetEntity> findAll();
}
