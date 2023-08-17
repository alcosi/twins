package org.cambium.i18n.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface I18nRepository extends CrudRepository<I18nEntity, UUID>, JpaSpecificationExecutor<I18nEntity> {
    I18nEntity findByKey(String key);
}
