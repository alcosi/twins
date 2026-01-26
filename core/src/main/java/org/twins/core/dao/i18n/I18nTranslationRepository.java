package org.twins.core.dao.i18n;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface I18nTranslationRepository extends CrudRepository<I18nTranslationEntity, UUID>, JpaSpecificationExecutor<I18nTranslationEntity> {
    String CACHE_I18N_TRANSLATIONS = "I18nTranslationRepository.findByI18nIdAndLocale";

    @Cacheable(value = CACHE_I18N_TRANSLATIONS, key = "#i18nId + '' + #locale ")
    Optional<I18nTranslationEntity> findByI18nIdAndLocale(UUID i18nId, Locale locale);

    List<I18nTranslationEntity> findByI18nAndLocaleIn(I18nEntity i18n, List<Locale> locales);

    List<I18nTranslationEntity> findByI18nIdAndLocaleIn(UUID i18nId, Collection<Locale> locales);

    List<I18nTranslationEntity> findByI18nId(UUID i18nId);
    List<I18nTranslationEntity> findByI18nIdIn(Collection<UUID> i18nIds);
    List<I18nTranslationNoRelationsProjection> findByI18nIdInAndLocale(Collection<UUID> i18nIds, Locale locale);
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
    @Query(nativeQuery = true, value = "insert into i18n_translation (i18n_id, locale, translation, usage_counter) " +
            "values (:i18nId, :localeCode, '', 1) on conflict on constraint i18n_translation_uq do " +
            "update set usage_counter = excluded.usage_counter + 1")
    void incrementUsageCounter(@Param("i18nId") UUID i18nId, @Param("localeCode") String localeCode);
}
