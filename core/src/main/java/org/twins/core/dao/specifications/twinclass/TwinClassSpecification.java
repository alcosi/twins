package org.twins.core.dao.specifications.twinclass;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.ArrayUtils;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinClassSpecification extends CommonSpecification<TwinClassEntity> {

    public static <T> Specification<T> checkExtendsTwinClassChildren(Collection<UUID> ids, boolean not,
                                                                     boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkHierarchyChildren(ids, not, includeNullValues, depthLimit, ArrayUtils.concatArray(twinClassFieldPath, TwinClassEntity.Fields.extendsHierarchyTree));
    }

    public static <T> Specification<T> checkHeadTwinClassChildren(Collection<UUID> ids, boolean not,
                                                                  boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkHierarchyChildren(ids, not, includeNullValues, depthLimit, ArrayUtils.concatArray(twinClassFieldPath, TwinClassEntity.Fields.headHierarchyTree));
    }


    public static <T> Specification<T> checkExtendsTwinClassParents(Collection<UUID> ids, boolean not,
                                                                    boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkHierarchyParent(ids, not, includeNullValues, depthLimit, TwinClassEntity.Fields.extendsHierarchyTree, TwinClassEntity.Fields.id, TwinClassEntity.class, twinClassFieldPath);
    }

    public static <T> Specification<T> checkHeadTwinClassParents(Collection<UUID> ids, boolean not,
                                                                 boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkHierarchyParent(ids, not, includeNullValues, depthLimit, TwinClassEntity.Fields.headHierarchyTree, TwinClassEntity.Fields.id, TwinClassEntity.class, twinClassFieldPath);
    }



    public static Specification<TwinClassEntity> checkHierarchyIsChild(String field, final UUID id) {
        return (root, query, cb) -> {
            String ltreeId = LTreeUtils.matchInTheMiddle(id);
            Expression<String> hierarchyTreeExpression = root.get(field);
            return cb.isTrue(cb.function("hierarchy_check_lquery", Boolean.class, hierarchyTreeExpression, cb.literal(ltreeId)));
        };
    }

    public static Specification<TwinClassEntity> checkOwnerTypeIn(final Collection<OwnerType> ownerTypes, final boolean not) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(ownerTypes)) {
                for (OwnerType ownerType : ownerTypes) {
                    Predicate predicate = cb.equal(root.get(TwinClassEntity.Fields.ownerType), ownerType);
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, true);
        };
    }

    //todo maybe delete
    public static Specification<TwinClassEntity> hasOwnerType(OwnerType ownerType) {
        return (root, query, cb) -> {
            if (ownerType == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get(TwinClassEntity.Fields.ownerType), ownerType);
        };
    }
}
