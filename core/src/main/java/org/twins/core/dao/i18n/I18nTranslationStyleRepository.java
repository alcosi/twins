package org.twins.core.dao.i18n;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public interface I18nTranslationStyleRepository extends CrudRepository<I18nTranslationStyleEntity, String>, JpaSpecificationExecutor<I18nTranslationStyleEntity> {
    List<I18nTranslationStyleEntity> findByI18nIdAndLocale(UUID i18nId, Locale locale);

    void deleteAllByI18nIdAndLocale(UUID i18nId, Locale locale);
}
