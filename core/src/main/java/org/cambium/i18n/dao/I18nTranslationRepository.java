package org.cambium.i18n.dao;

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
public interface I18nTranslationRepository extends CrudRepository<I18nTranslationEntity, UUID>, JpaSpecificationExecutor<I18nTranslationEntity> {
    Optional<I18nTranslationEntity> findByI18nAndLocale(I18nEntity i18n, Locale locale);

    List<I18nTranslationEntity> findByI18nAndLocaleIn(I18nEntity i18n, List<Locale> locales);

    List<I18nTranslationEntity> findByI18nId(UUID i18nId);
    Optional<I18nTranslationEntity> findByLocaleAndI18n_Key(Locale locale, String messageKey);

    List<I18nTranslationEntity> findAll();

    List<I18nTranslationEntity> findByI18nOrderByLocale(I18nEntity i18n);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "insert into i18n_translations " +
            "select i18n.id, :localeCode, '', 0 " +
            "from i18n where i18n_type_id in (:typesList) on conflict do nothing;")
    void refresh(@Param("localeCode") String localeCode, @Param("typesList") List<Integer> typesList);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update i18n_translations " +
            "set usage_counter = usage_counter + 1 " +
            "where locale = :localeCode " +
            "and i18n_id = :i18nId")
    void incrementUsageCounter(@Param("i18nId") UUID i18nId, @Param("localeCode") String localeCode);
}
