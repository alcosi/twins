package org.cambium.i18n.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface I18nLocaleRepository extends CrudRepository<I18nLocaleEntity, String>, JpaSpecificationExecutor<I18nLocaleEntity> {
    I18nLocaleEntity getByLocale(String languageTag);
}
