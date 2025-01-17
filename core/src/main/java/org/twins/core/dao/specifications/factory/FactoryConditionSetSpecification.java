package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class FactoryConditionSetSpecification extends CommonSpecification<TwinFactoryConditionSetEntity> {


    public static Specification<TwinFactoryConditionSetEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(TwinFactoryConditionSetEntity.Fields.domainId), domainId);
        };
    }

}
