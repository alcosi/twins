package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.usergroup.UserGroupMapEntity;

import java.util.Collection;
import java.util.UUID;

public class DomainBusinessAccountUserSpecification extends CommonSpecification<DomainBusinessAccountUserEntity> {

    public static Specification<DomainBusinessAccountUserEntity> checkUserGroupIdIn(Collection<UUID> userGroupIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(userGroupIds))
                return cb.conjunction();

            query.distinct(true);
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<UserGroupMapEntity> subRoot = subquery.from(UserGroupMapEntity.class);
            subquery.select(subRoot.get(UserGroupMapEntity.Fields.userId));
            subquery.where(
                    cb.and(
                            subRoot.get(UserGroupMapEntity.Fields.userGroupId).in(userGroupIds),
                            cb.equal(subRoot.get(UserGroupMapEntity.Fields.domainId), root.get(DomainBusinessAccountUserEntity.Fields.domainId)),
                            cb.or(
                                    cb.isNull(subRoot.get(UserGroupMapEntity.Fields.businessAccountId)),
                                    cb.equal(subRoot.get(UserGroupMapEntity.Fields.businessAccountId), root.get(DomainBusinessAccountUserEntity.Fields.businessAccountId))
                            ),
                            cb.or(
                                    cb.isTrue(subRoot.get(UserGroupMapEntity.Fields.addedManually)),
                                    cb.gt(subRoot.get(UserGroupMapEntity.Fields.involvesCount), 0)
                            )
                    )
            );
            Predicate predicate = root.get(DomainBusinessAccountUserEntity.Fields.userId).in(subquery);
            return not ? cb.not(predicate) : predicate;
        };
    }
}
