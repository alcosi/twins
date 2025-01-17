package org.twins.core.dao.specifications.twinflow;

import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;


import java.util.UUID;


public class TwinflowSchemaSpecification extends CommonSpecification<TwinflowSchemaEntity> {


    public static Specification<TwinflowSchemaEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(PermissionGroupEntity.Fields.domainId), domainId);
        };
    }
}
