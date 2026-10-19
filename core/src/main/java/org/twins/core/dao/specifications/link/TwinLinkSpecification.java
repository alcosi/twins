package org.twins.core.dao.specifications.link;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.enums.link.LinkStrength;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TwinLinkSpecification extends CommonSpecification<TwinLinkEntity> {

    public static Specification<TwinLinkEntity> checkStrength(final List<LinkStrength> strengthIds) {
        return (root, query, cb) -> {
            if (strengthIds == null || strengthIds.isEmpty()) return cb.conjunction();
            return root.join(TwinLinkEntity.Fields.linkSpecOnly).get(LinkEntity.Fields.linkStrengthId).in(strengthIds);
        };
    }

    /**
     * Matches twin links whose source OR destination twin is in the given set.
     * With {@code not = true} this becomes "neither endpoint is in the set" (exclude semantics).
     */
    public static Specification<TwinLinkEntity> checkSrcOrDstTwinIdIn(final Collection<UUID> twinIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(twinIds))
                return cb.conjunction();
            Predicate in = cb.or(
                    root.get(TwinLinkEntity.Fields.srcTwinId).in(twinIds),
                    root.get(TwinLinkEntity.Fields.dstTwinId).in(twinIds)
            );
            return not ? cb.not(in) : in;
        };
    }
}
