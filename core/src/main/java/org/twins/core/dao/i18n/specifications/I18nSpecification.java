package org.twins.core.dao.i18n.specifications;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class I18nSpecification<T> {

    public static <T> Specification<T> joinAndSearchByI18NField(final String fieldName, final Collection<String> search, final Locale locale, final boolean or, final boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            // Create joins for I18nEntity and I18nTranslationEntity
            Join<T, I18nEntity> i18nJoin = root.join(fieldName, JoinType.LEFT);
            Join<I18nEntity, I18nTranslationEntity> translationJoin = i18nJoin.join(I18nEntity.Fields.translations, JoinType.LEFT);

            // Create search predicates
            Predicate localePredicate = cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale);
            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            Predicate searchPredicate = getPredicate(cb, likePredicates, or);

            // Return main condition, checking the existence of translations
            return cb.and(localePredicate, searchPredicate);
        };
    }

    public static <T, S> Specification<T> doubleJoinAndSearchByI18NField(final String fieldJoin, final String fieldName, final Collection<String> search, final Locale locale, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            // Create joins for I18nEntity and I18nTranslationEntity
            Join<T, S> tableJoin = root.join(fieldJoin, JoinType.LEFT);
            Join<T, I18nEntity> i18nJoin = tableJoin.join(fieldName, JoinType.LEFT);
            Join<I18nEntity, I18nTranslationEntity> translationJoin = i18nJoin.join(I18nEntity.Fields.translations, JoinType.LEFT);

            // Create search predicates
            Predicate localePredicate = cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale);
            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            Predicate searchPredicate = getPredicate(cb, likePredicates, or);

            // Return main condition, checking the existence of translations
            return cb.and(localePredicate, searchPredicate);
        };
    }

    private static List<Predicate> buildLikePredicates(CriteriaBuilder cb, Path<String> path, Collection<String> search, boolean not) {
        List<Predicate> predicates = new ArrayList<>();
        for (String value : search) {
            predicates.add(not ?
                    cb.notLike(cb.lower(path), "%" + value.toLowerCase() + "%", CommonSpecification.escapeChar) :
                    cb.like(cb.lower(path), "%" + value.toLowerCase() + "%", CommonSpecification.escapeChar));
        }
        return predicates;
    }
}
