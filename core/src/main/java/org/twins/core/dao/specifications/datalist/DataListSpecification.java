package org.twins.core.dao.specifications.datalist;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class DataListSpecification extends CommonSpecification<DataListEntity> {

    public static Specification<DataListEntity> checkDataListOptionUuidIn(String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids))
                return cb.conjunction();

            Join<DataListEntity, ?> joinDataListOption = getOrCreateJoin(root);

            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(joinDataListOption.get(uuidField).in(uuids).not(), joinDataListOption.get(uuidField).isNull())
                            : joinDataListOption.get(uuidField).in(uuids).not())
                    : joinDataListOption.get(uuidField).in(uuids);
        };
    }

    public static Specification<DataListEntity> checkDataListFieldLikeIn(String field, Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String value : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), value.toLowerCase());
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DataListEntity> checkDataListOptionFieldLikeIn(String field, Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String value : search) {
                    Predicate predicate = cb.like(cb.lower(getOrCreateJoin(root).get(field)), value.toLowerCase());
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Join<DataListEntity, ?> getOrCreateJoin(Root<DataListEntity> root) {
        return root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(DataListEntity.Fields.dataListOptions))
                .findFirst()
                .orElseGet(() -> root.join(DataListEntity.Fields.dataListOptions, JoinType.LEFT));
    }

    public static Specification<DataListEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(DataListEntity.Fields.domainId), domainId);
        };
    }
}
