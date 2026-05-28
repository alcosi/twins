package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.domain.search.DomainBusinessAccountUserSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;

import java.util.Collection;
import java.util.UUID;

public class DomainBusinessAccountUserSpecification extends CommonSpecification<DomainBusinessAccountUserEntity> {

    public static Specification<DomainBusinessAccountUserEntity> checkUserGroupIdIn(Collection<UUID> userGroupIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(userGroupIds))
                return cb.conjunction();

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

    public static Specification<DomainBusinessAccountUserEntity> createSortSpecification(DomainBusinessAccountUserSearch search) {
        DomainBusinessAccountUserSortField sortField = search.getSortField();
        if (sortField == null)
            sortField = DomainBusinessAccountUserSortField.createdAt;
        boolean ascending = search.getSortDirection() != SortDirection.DESC;
        String[] fieldPath = switch (sortField) {
            case createdAt -> new String[]{DomainBusinessAccountUserEntity.Fields.createdAt};
            case lastActivityAt -> new String[]{DomainBusinessAccountUserEntity.Fields.lastActivityAt};
            case userName -> new String[]{DomainBusinessAccountUserEntity.Fields.user, UserEntity.Fields.name};
            case businessAccountName -> new String[]{DomainBusinessAccountUserEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name};
        };
        return toSortSpecification(fieldPath, ascending);
    }
}
