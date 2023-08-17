package org.cambium.i18n.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface I18nLocaleRepository extends CrudRepository<I18nLocaleEntity, UUID>, JpaSpecificationExecutor<I18nLocaleEntity> {
    List<I18nLocaleEntity> findAll();
}
