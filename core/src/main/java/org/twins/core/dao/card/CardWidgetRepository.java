package org.twins.core.dao.card;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardWidgetRepository extends CrudRepository<CardWidgetEntity, UUID>, JpaSpecificationExecutor<CardWidgetEntity> {
    List<CardWidgetEntity> findByCardId(UUID cardId);
}
