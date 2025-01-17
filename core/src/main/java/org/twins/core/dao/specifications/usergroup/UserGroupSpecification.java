package org.twins.core.dao.specifications.usergroup;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.user.UserGroupEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class UserGroupSpecification extends CommonSpecification<UserGroupEntity> {


    //todo this method can be moved to the common specification with constant field name "domainId"
    public static Specification<UserGroupEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(UserGroupEntity.Fields.domainId), domainId);
        };
    }
}
