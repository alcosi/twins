package org.twins.core.dao.specifications.link;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.List;

@Slf4j
public class TwinLinkSpecification extends CommonSpecification<TwinLinkEntity> {

    public static Specification<TwinLinkEntity> checkStrength(final List<LinkStrength> strengthIds) {
        return (root, query, cb) -> {
            if (strengthIds == null || strengthIds.isEmpty()) return cb.conjunction();
            return root.join(TwinLinkEntity.Fields.link).get(LinkEntity.Fields.linkStrengthId).in(strengthIds);
        };
    }


}
