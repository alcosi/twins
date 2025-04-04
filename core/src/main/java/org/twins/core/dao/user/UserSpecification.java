package org.twins.core.dao.user;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.domain.search.SpaceSearch;
import org.twins.core.dto.rest.user.SpaceSearchDTOv1;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class UserSpecification extends CommonSpecification<UserEntity> {
    public static Specification<UserEntity> checkUserDomain(UUID domainId) {
        return (root, query, cb) -> {
            if (query == null || domainId == null) {
                return cb.conjunction();
            }

            Subquery<DomainUserEntity> domainSubquery = query.subquery(DomainUserEntity.class);
            Root<DomainUserEntity> domainUserRoot = domainSubquery.from(DomainUserEntity.class);

            Predicate userMatch = cb.equal(
                    domainUserRoot.get(DomainUserEntity.Fields.userId),
                    root.get(UserEntity.Fields.id)
            );

            Predicate domainMatch = cb.equal(
                    domainUserRoot.get(DomainUserEntity.Fields.domainId),
                    domainId
            );

            domainSubquery.select(domainUserRoot)
                    .where(cb.and(userMatch, domainMatch));

            return cb.exists(domainSubquery);
        };
    }

    public static Specification<UserEntity> checkStatusLikeIn(Set<UserStatus> statuses, boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(statuses)) {
                return cb.conjunction();
            }

            return exclude
                    ? cb.not(root.get(UserEntity.Fields.userStatusId).in(statuses))
                    : root.get(UserEntity.Fields.userStatusId).in(statuses);
        };
    }

    public static Specification<UserEntity> checkSpaceRoleLikeIn(
            List<SpaceSearch> spaceRoles,
            UUID domainId,
            UUID businessAccountId,
            boolean exclude
    ) {
        return (root, query, cb) -> {
            if (query == null || CollectionUtils.isEmpty(spaceRoles)) {
                return cb.conjunction();
            }

            Subquery<SpaceRoleUserEntity> userSubquery = query.subquery(SpaceRoleUserEntity.class);
            Root<SpaceRoleUserEntity> userRoleRoot = userSubquery.from(SpaceRoleUserEntity.class);

            Predicate userMatch = cb.equal(
                    userRoleRoot.get(SpaceRoleUserEntity.Fields.userId),
                    root.get(UserEntity.Fields.id)
            );

            Subquery<SpaceRoleUserGroupEntity> groupSubquery = query.subquery(SpaceRoleUserGroupEntity.class);
            Root<SpaceRoleUserGroupEntity> groupRoleRoot = groupSubquery.from(SpaceRoleUserGroupEntity.class);

            Expression<UUID> functionCall = cb.function(
                    "get_users_by_groups",
                    UUID.class,
                    cb.literal(groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.userGroupId)),
                    cb.literal(domainId),
                    cb.literal(businessAccountId)
            );

            Predicate userInGroups = cb.in(root.get(UserEntity.Fields.id)).value(functionCall);

            Predicate[] rolePredicates = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .map(spaceRole -> {
                        Predicate conditions = cb.conjunction();

                        if (spaceRole.getSpaceId() != null) {
                            Predicate userSpaceMatch = cb.equal(
                                    userRoleRoot.get(SpaceRoleUserEntity.Fields.twinId),
                                    spaceRole.getSpaceId()
                            );

                            Predicate groupSpaceMatch = cb.equal(
                                    groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.twinId),
                                    spaceRole.getSpaceId()
                            );

                            conditions = cb.and(conditions, cb.or(userSpaceMatch, groupSpaceMatch));
                        }

                        if (spaceRole.getRoleId() != null) {
                            Predicate userRoleMatch = cb.equal(
                                    userRoleRoot.get(SpaceRoleUserEntity.Fields.spaceRoleId),
                                    spaceRole.getRoleId()
                            );

                            Predicate groupRoleMatch = cb.equal(
                                    groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.spaceRoleId),
                                    spaceRole.getRoleId()
                            );

                            conditions = cb.and(conditions, cb.or(userRoleMatch, groupRoleMatch));
                        }

                        return conditions;
                    })
                    .filter(p -> !p.equals(cb.conjunction()))
                    .toArray(Predicate[]::new);

            if (rolePredicates.length == 0) {
                return cb.conjunction();
            }

            userSubquery.select(userRoleRoot)
                    .where(cb.and(userMatch, cb.or(rolePredicates)));

            groupSubquery.select(groupRoleRoot)
                    .where(cb.and(cb.or(rolePredicates)));

            Predicate combinedCondition = cb.or(
                    cb.exists(userSubquery),
                    cb.and(cb.exists(groupSubquery), userInGroups)
            );

            return exclude ? cb.not(combinedCondition) : combinedCondition;
        };
    }
}
