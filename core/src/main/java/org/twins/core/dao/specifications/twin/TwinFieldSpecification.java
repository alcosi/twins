package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;

import java.util.*;

import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
public class TwinFieldSpecification {
    private TwinFieldSpecification() {
    }

    public static Specification<TwinFieldEntity> getCalcChildrenFieldSpecification(TwinFieldEntity twinFieldEntity, boolean statusIn, UUID uuid, Set<UUID> uuids) {
        return where(
                checkUuidTwinJoin(TwinEntity.Fields.headTwinId, twinFieldEntity.getTwinId(), false)
                        .and(checkUuid(TwinFieldEntity.Fields.twinClassFieldId, uuid, false))
                        .and(checkUuidInTwinJoin(TwinEntity.Fields.twinStatusId, uuids, statusIn))
        );
    }

    public static Specification<TwinFieldEntity> dummy() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<TwinFieldEntity> checkUuid(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> not ? cb.equal(root.get(uuidField), uuid).not() : cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<TwinFieldEntity> checkUuidTwinJoin(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> {
            Join<TwinFieldEntity, TwinEntity> twinJoin = root.join(TwinFieldEntity.Fields.twin);
            return not ? cb.equal(twinJoin.get(uuidField), uuid).not() : cb.equal(twinJoin.get(uuidField), uuid);
        };
    }

    public static Specification<TwinFieldEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(uuidField).in(uuids).not() : root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinFieldEntity> checkUuidInTwinJoin(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            Join<TwinFieldEntity, TwinEntity> twinJoin = root.join(TwinFieldEntity.Fields.twin);
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? twinJoin.get(uuidField).in(uuids).not() : twinJoin.get(uuidField).in(uuids);
        };
    }
}
