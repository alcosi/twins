package org.twins.core.dao.i18n;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface I18nTranslationBinRepository extends CrudRepository<I18nTranslationBinEntity, UUID>, JpaSpecificationExecutor<I18nTranslationBinEntity> {
    Optional<I18nTranslationBinEntity> findByI18nAndLocale(I18nEntity i18n, Locale locale);

    List<I18nTranslationBinEntity> findByI18nAndLocaleIn(I18nEntity i18n, List<Locale> locale);

    Optional<I18nTranslationBinEntity> findByI18nIdAndLocale(UUID i18nId, Locale locale);

    Optional<I18nTranslationBinEntity> findByLocaleAndI18nKey(Locale locale, String messageKey);

    List<I18nTranslationBinEntity> findAll();

    List<I18nTranslationBinEntity> findByI18nOrderByLocale(I18nEntity i18n);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "insert into i18n_translations_bin " +
            "select i18n.id, :localeCode, ''" +
            "from i18n where i18n_type_id in (:typesList) on conflict do nothing;")
    void refresh(@Param("localeCode") String localeCode, @Param("typesList") List<Integer> typesList);
}
