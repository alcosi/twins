package org.twins.core.dao.i18n.specifications;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class I18nSpecification<T> {

    /**
     * Legacy: joins through {@code @ManyToOne xxxI18n} -> {@code @OneToMany translations}.
     * Produces two LEFT JOINs (i18n + i18n_translation).
     * Use {@link #joinAndSearchByI18NFieldDirect} when the source entity exposes a
     * {@code @OneToMany xxxI18nTranslationsSpecOnly} association — that variant skips the
     * intermediate {@code i18n} table.
     */
    public static <T> Specification<T> joinAndSearchByI18NField(final String fieldName, final Collection<String> search, final Locale locale, final boolean or, final boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            Join<T, I18nEntity> i18nJoin = root.join(fieldName, JoinType.LEFT);
            Join<I18nEntity, I18nTranslationEntity> translationJoin = i18nJoin.join(I18nEntity.Fields.translations, JoinType.LEFT);

            Predicate localePredicate = cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale);
            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            Predicate searchPredicate = getPredicate(cb, likePredicates, or);

            return cb.and(localePredicate, searchPredicate);
        };
    }

    /**
     * Direct join to {@code i18n_translation} via {@code @OneToMany xxxI18nTranslationsSpecOnly}
     * association on the root entity. Skips the intermediate {@code i18n} table.
     */
    public static <T> Specification<T> joinAndSearchByI18NFieldDirect(final String translationsFieldName, final Collection<String> search, final Locale locale, final boolean or, final boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            Join<T, I18nTranslationEntity> translationJoin = root.join(translationsFieldName, JoinType.LEFT);
            translationJoin.on(cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale));

            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            return getPredicate(cb, likePredicates, or);
        };
    }

    /**
     * Legacy: double join via nested entity's {@code @ManyToOne xxxI18n} -> {@code @OneToMany translations}.
     * Produces three LEFT JOINs.
     */
    public static <T, S> Specification<T> doubleJoinAndSearchByI18NField(final String fieldJoin, final String fieldName, final Collection<String> search, final Locale locale, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            Join<T, S> tableJoin = root.join(fieldJoin, JoinType.LEFT);
            Join<T, I18nEntity> i18nJoin = tableJoin.join(fieldName, JoinType.LEFT);
            Join<I18nEntity, I18nTranslationEntity> translationJoin = i18nJoin.join(I18nEntity.Fields.translations, JoinType.LEFT);

            Predicate localePredicate = cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale);
            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            Predicate searchPredicate = getPredicate(cb, likePredicates, or);

            return cb.and(localePredicate, searchPredicate);
        };
    }

    /**
     * Direct double join: navigate to the holder entity, then join {@code i18n_translation} directly
     * via {@code @OneToMany xxxI18nTranslationsSpecOnly}. Produces two LEFT JOINs instead of three.
     */
    public static <T, S> Specification<T> doubleJoinAndSearchByI18NFieldDirect(final String fieldJoin, final String translationsFieldName, final Collection<String> search, final Locale locale, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) return cb.conjunction();

            Join<T, S> tableJoin = root.join(fieldJoin, JoinType.LEFT);
            Join<S, I18nTranslationEntity> translationJoin = tableJoin.join(translationsFieldName, JoinType.LEFT);
            translationJoin.on(cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale));

            List<Predicate> likePredicates = buildLikePredicates(cb, translationJoin.get(I18nTranslationEntity.Fields.translation), search, not);
            return getPredicate(cb, likePredicates, or);
        };
    }

    /**
     * Legacy sort: navigates through {@code @ManyToOne xxxI18n} -> {@code @OneToMany translations}.
     * Produces two LEFT JOINs on the leaf step.
     */
    public static <T> Specification<T> toSortSpecification(boolean ascending, Locale locale, String... fieldPath) {
        if (fieldPath == null || fieldPath.length == 0 || locale == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> {
            if (query.getResultType().equals(Long.class))
                return cb.conjunction();
            From<?, ?> current = root;
            for (int i = 0; i < fieldPath.length - 1; i++) {
                current = findOrCreateJoin(current, fieldPath[i], JoinType.LEFT);
            }
            Join<?, ?> i18nJoin = findOrCreateJoin(current, fieldPath[fieldPath.length - 1], JoinType.LEFT);
            Join<?, ?> translationJoin = i18nJoin.join(I18nEntity.Fields.translations, JoinType.LEFT);
            translationJoin.on(cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale));
            Path<String> translationPath = translationJoin.get(I18nTranslationEntity.Fields.translation);
            List<Order> orders = new ArrayList<>(query.getOrderList());
            orders.add(ascending ? cb.asc(translationPath) : cb.desc(translationPath));
            query.orderBy(orders);
            return cb.conjunction();
        };
    }

    /**
     * Direct sort: the last segment of {@code fieldPath} must point to a {@code @OneToMany}
     * association of type {@link I18nTranslationEntity} (e.g. {@code nameI18nTranslationsSpecOnly}).
     * All previous segments are normal associations to navigate to the holder entity.
     * Produces a single LEFT JOIN to {@code i18n_translation} on the leaf step (no intermediate {@code i18n}).
     */
    public static <T> Specification<T> toSortSpecificationDirect(boolean ascending, Locale locale, String... fieldPath) {
        if (fieldPath == null || fieldPath.length == 0 || locale == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> {
            if (query.getResultType().equals(Long.class))
                return cb.conjunction();
            From<?, ?> current = root;
            for (int i = 0; i < fieldPath.length - 1; i++) {
                current = findOrCreateJoin(current, fieldPath[i], JoinType.LEFT);
            }
            Join<?, ?> translationJoin = findOrCreateJoin(current, fieldPath[fieldPath.length - 1], JoinType.LEFT);
            translationJoin.on(cb.equal(translationJoin.get(I18nTranslationEntity.Fields.locale), locale));
            Path<String> translationPath = translationJoin.get(I18nTranslationEntity.Fields.translation);
            List<Order> orders = new ArrayList<>(query.getOrderList());
            orders.add(ascending ? cb.asc(translationPath) : cb.desc(translationPath));
            query.orderBy(orders);
            return cb.conjunction();
        };
    }

    private static Join<?, ?> findOrCreateJoin(From<?, ?> from, String attribute, JoinType joinType) {
        for (Join<?, ?> join : from.getJoins()) {
            Attribute<?, ?> attr = (Attribute<?, ?>) join.getAttribute();
            if (attr != null && attr.getName().equals(attribute)) {
                return join;
            }
        }
        return from.join(attribute, joinType);
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
