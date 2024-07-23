package org.cambium.i18n.dao.specifications;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.cambium.i18n.dao.I18nTranslationEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class I18nSpecification<T> {

    public static <T> Specification<T> joinAndSearchByI18NField(final String fieldName, final Collection<String> search, final Locale locale, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();
            // create subquery
            Subquery<UUID> i18nSubquery = query.subquery(UUID.class);
            Root<I18nTranslationEntity> i18nRoot = i18nSubquery.from(I18nTranslationEntity.class);
            // select i18nIds by locale and search
            Predicate localePredicate = cb.equal(i18nRoot.get(I18nTranslationEntity.Fields.locale), locale);
            List<Predicate> likePredicates = buildLikePredicates(cb, i18nRoot.get(I18nTranslationEntity.Fields.translation), search);
            Predicate searchPredicate = getPredicate(cb, likePredicates, or);
            i18nSubquery.select(i18nRoot.get(I18nTranslationEntity.Fields.i18nId)).where(cb.and(localePredicate, searchPredicate));
            // return main condition, checking the occurrence of the i18nId field in the subquery
            return root.get(fieldName).in(i18nSubquery);
        };
    }

    private static List<Predicate> buildLikePredicates(CriteriaBuilder cb, Path<String> path, Collection<String> search) {
        List<Predicate> predicates = new ArrayList<>();
            for (String value : search)
                predicates.add(cb.like(cb.lower(path), "%" + value.toLowerCase() + "%"));
        return predicates;
    }
}
