package org.twins.core.dao.specifications.twin_class;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinClassSpecification {

    public static Specification<TwinClassEntity> checkHierarchyIsChild(String field, final UUID id) {
        return (root, query, cb) -> {
            String ltreeId = LTreeUtils.matchInTheMiddle(id);
            Expression<String> hierarchyTreeExpression = root.get(field);
            return cb.isTrue(cb.function("hierarchy_check_lquery", Boolean.class, hierarchyTreeExpression, cb.literal(ltreeId)));
        };
    }

    public static Specification<TwinClassEntity> checkUuidIn(final String fieldName, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(fieldName).in(uuids).not() : root.get(fieldName).in(uuids);
        };
    }

    public static Specification<TwinClassEntity> checkFieldLikeIn(final String fieldName, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search))
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(fieldName)), s.toLowerCase());
                    predicates.add(predicate);
                }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinClassEntity> hasOwnerType(TwinClassEntity.OwnerType ownerType) {
        return (root, query, cb) -> {
            if (ownerType == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get(TwinClassEntity.Fields.ownerType), ownerType);
        };
    }

    public static Specification<TwinClassEntity> checkTernary(final String fieldName, final Ternary ternary) {
        return (root, query, cb) -> {
            if (ternary == null) return cb.conjunction();
            switch (ternary) {
                case ONLY:
                    return cb.isTrue(root.get(fieldName));
                case ONLY_NOT:
                    return cb.isFalse(root.get(fieldName));
                case ANY:
                default:
                    return cb.conjunction();
            }
        };
    }
}
