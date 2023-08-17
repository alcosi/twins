package org.cambium.i18n.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;

@Repository
public interface I18nTranslationStyleRepository extends CrudRepository<I18nTranslationStyleEntity, String>, JpaSpecificationExecutor<I18nTranslationStyleEntity> {
    List<I18nTranslationStyleEntity> findByI18nAndLocale(I18nEntity i18n, Locale locale);

    void deleteAllByI18nAndLocale(I18nEntity i18n, Locale locale);
}
