package org.twins.core.dao.i18n.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class I18nTranslationSpecification extends CommonSpecification<I18nTranslationEntity> {
    public static Specification<I18nTranslationEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<I18nTranslationEntity, I18nEntity> i18nJoin = root.join(I18nTranslationEntity.Fields.i18n, JoinType.INNER);

            return cb.equal(i18nJoin.get(I18nEntity.Fields.domainId), domainId);
        };
    }

    public static Specification<I18nTranslationEntity> checkLocaleIn(Set<Locale> locales, boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(locales)) {
                return null;
            }
            if (exclude) {
                return cb.not(root.get(I18nTranslationEntity.Fields.locale).in(locales));
            } else {
                return root.get(I18nTranslationEntity.Fields.locale).in(locales);
            }
        };
    }
}
