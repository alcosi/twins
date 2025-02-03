package org.twins.core.dao.specifications.link;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class LinkSpecification extends CommonSpecification<TwinFactoryEntity> {


    public static Specification<LinkEntity> checkSrcOrDstTwinClassIdIn(Collection<UUID> search, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            Predicate srcTwinClassIdPredicate = root.get(LinkEntity.Fields.srcTwinClassId).in(search);
            Predicate dstTwinClassIdPredicate = root.get(LinkEntity.Fields.dstTwinClassId).in(search);

            Predicate predicate = cb.or(srcTwinClassIdPredicate, dstTwinClassIdPredicate);
            if (not) predicate = cb.not(predicate);
            return predicate;
        };
    }


}
