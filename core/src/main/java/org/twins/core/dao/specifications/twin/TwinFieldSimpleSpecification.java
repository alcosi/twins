package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.Join;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
public class TwinFieldSimpleSpecification extends CommonSpecification<TwinFieldSimpleEntity> {
    private TwinFieldSimpleSpecification() {
    }

    public static Specification<TwinFieldSimpleEntity> getCalcChildrenFieldSpecification(TwinFieldSimpleEntity twinFieldEntity, boolean statusIn, UUID uuid, Set<UUID> uuids) {
        return where(
                checkUuidTwinJoin(TwinEntity.Fields.headTwinId, twinFieldEntity.getTwinId(), false)
                        .and(checkUuid(TwinFieldSimpleEntity.Fields.twinClassFieldId, uuid, false))
                        .and(checkUuidInTwinJoin(TwinEntity.Fields.twinStatusId, uuids, statusIn))
        );
    }

    public static Specification<TwinFieldSimpleEntity> dummy() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<TwinFieldSimpleEntity> checkUuid(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> not ? cb.equal(root.get(uuidField), uuid).not() : cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<TwinFieldSimpleEntity> checkUuidTwinJoin(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> {
            Join<TwinFieldSimpleEntity, TwinEntity> twinJoin = root.join(TwinFieldSimpleEntity.Fields.twin);
            return not ? cb.equal(twinJoin.get(uuidField), uuid).not() : cb.equal(twinJoin.get(uuidField), uuid);
        };
    }

    public static Specification<TwinFieldSimpleEntity> checkUuidInTwinJoin(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            Join<TwinFieldSimpleEntity, TwinEntity> twinJoin = root.join(TwinFieldSimpleEntity.Fields.twin);
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? twinJoin.get(uuidField).in(uuids).not() : twinJoin.get(uuidField).in(uuids);
        };
    }
}
