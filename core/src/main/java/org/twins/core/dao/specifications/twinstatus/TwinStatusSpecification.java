package org.twins.core.dao.specifications.twinstatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class TwinStatusSpecification extends CommonSpecification<TwinStatusEntity> {


    public static Specification<TwinStatusEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<TwinStatusEntity, TwinClassEntity> joinTwinClass = root.join(TwinStatusEntity.Fields.twinClass, JoinType.INNER);
            return cb.equal(joinTwinClass.get(TwinClassEntity.Fields.domainId), domainId);
        };
    }
}
