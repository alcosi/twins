package org.twins.core.dao.specifications.link;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TwinLinkSpecification {

    public static Specification<TwinLinkEntity> checkUuid(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> not ? cb.equal(root.get(uuidField), uuid).not() : cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<TwinLinkEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(uuidField).in(uuids).not() : root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinLinkEntity> checkStrength(final List<LinkStrength> strengthIds) {
        return (root, query, cb) -> {
            if (strengthIds == null || strengthIds.isEmpty()) return cb.conjunction();
            return root.join(TwinLinkEntity.Fields.link).get(LinkEntity.Fields.linkStrengthId).in(strengthIds);
        };
    }


}
