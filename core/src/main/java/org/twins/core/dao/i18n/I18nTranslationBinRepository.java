package org.twins.core.dao.i18n;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface I18nTranslationBinRepository extends CrudRepository<I18nTranslationBinEntity, UUID>, JpaSpecificationExecutor<I18nTranslationBinEntity> {
    List<I18nTranslationBinEntity> findByI18nIdAndLocaleIn(UUID i18nId, Collection<Locale> locale);

    Optional<I18nTranslationBinEntity> findByI18nIdAndLocale(UUID i18nId, Locale locale);

    List<I18nTranslationBinEntity> findAll();

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "insert into i18n_translations_bin " +
            "select i18n.id, :localeCode, ''" +
            "from i18n where i18n_type_id in (:typesList) on conflict do nothing;")
    void refresh(@Param("localeCode") String localeCode, @Param("typesList") List<Integer> typesList);
}
