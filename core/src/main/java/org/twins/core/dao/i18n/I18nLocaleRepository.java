package org.twins.core.dao.i18n;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface I18nLocaleRepository extends CrudRepository<I18nLocaleEntity, String>, JpaSpecificationExecutor<I18nLocaleEntity> {

    I18nLocaleEntity getByLocale(String languageTag);

    List<I18nLocaleEntity> findAllByActiveIsTrue();
}
