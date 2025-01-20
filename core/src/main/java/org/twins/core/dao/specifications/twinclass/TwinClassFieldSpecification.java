package org.twins.core.dao.specifications.twinclass;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinClassFieldSpecification extends CommonSpecification<TwinClassFieldEntity> {

    public static Specification<TwinClassFieldEntity> checkRequired(Ternary required) {
        return (root, query, cb) -> {
            if (required == null)
                return cb.conjunction();
            return switch (required) {
                case ONLY -> cb.isTrue(root.get(TwinClassFieldEntity.Fields.required));
                case ONLY_NOT -> cb.isFalse(root.get(TwinClassFieldEntity.Fields.required));
                default -> cb.conjunction();
            };
        };
    }

    public static Specification<TwinClassFieldEntity> checkFieldTyperIdIn(final Collection<Integer> ids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids))
                return cb.conjunction();
            String id = TwinClassFieldEntity.Fields.fieldTyperFeaturerId;
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(cb.not(root.get(id).in(ids)), root.get(id).isNull())
                            : cb.not(root.get(id).in(ids)))
                    : root.get(id).in(ids);
        };
    }

    public static Specification<TwinClassFieldEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            ArrayList<Predicate> predicates = new ArrayList<>();
            for (String name : search) {
                Predicate predicate = cb.like(cb.lower(root.get(field)), name.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinClassFieldEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<TwinClassFieldEntity, TwinClassEntity> twinClassJoin = root.join(TwinClassFieldEntity.Fields.twinClass, JoinType.INNER);
            return cb.equal(twinClassJoin.get(TwinClassEntity.Fields.domainId), domainId);
        };
    }
}
