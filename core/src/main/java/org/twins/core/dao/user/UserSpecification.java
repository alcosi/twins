package org.twins.core.dao.user;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.domain.search.SpaceSearch;
import org.twins.core.enums.user.UserStatus;

import java.util.*;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

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
            if (CollectionUtils.isEmpty(spaceRoles)) {
                return cb.conjunction();
            }

            // --- Join for direct user roles ---
            Join<UserEntity, SpaceRoleUserEntity> userRolesJoin = root.join(UserEntity.Fields.spaceRoleUsers, JoinType.LEFT);

            List<Predicate> directRolePredicates = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .map(spaceRole -> cb.and(
                            spaceRole.getSpaceId() != null
                                    ? cb.equal(userRolesJoin.get(SpaceRoleUserEntity.Fields.twinId), spaceRole.getSpaceId())
                                    : cb.conjunction(),
                            spaceRole.getRoleId() != null
                                    ? cb.equal(userRolesJoin.get(SpaceRoleUserEntity.Fields.spaceRoleId), spaceRole.getRoleId())
                                    : cb.conjunction()
                    ))
                    .toList();

            Predicate directRolesCombined = cb.or(directRolePredicates.toArray(new Predicate[0]));

            // --- Join for roles via groups ---
            Join<UserEntity, UserGroupMapEntity> ugmJoin = root.join(UserEntity.Fields.userGroupMaps, JoinType.LEFT);
            Join<UserGroupMapEntity, SpaceRoleUserGroupEntity> groupRolesJoin = ugmJoin.join(UserGroupMapEntity.Fields.spaceRoleUserGroups, JoinType.LEFT);

            List<Predicate> groupRolePredicates = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .map(spaceRole -> cb.and(
                            spaceRole.getSpaceId() != null
                                    ? cb.equal(groupRolesJoin.get(SpaceRoleUserGroupEntity.Fields.twinId), spaceRole.getSpaceId())
                                    : cb.conjunction(),
                            spaceRole.getRoleId() != null
                                    ? cb.equal(groupRolesJoin.get(SpaceRoleUserGroupEntity.Fields.spaceRoleId), spaceRole.getRoleId())
                                    : cb.conjunction()
                    ))
                    .toList();

            Predicate domainPredicate = domainId != null
                    ? cb.equal(ugmJoin.get(UserGroupMapEntity.Fields.domainId), domainId)
                    : cb.isNull(ugmJoin.get(UserGroupMapEntity.Fields.domainId));

            Predicate businessPredicate = businessAccountId != null
                    ? cb.or(
                    cb.isNull(ugmJoin.get(UserGroupMapEntity.Fields.businessAccountId)),
                    cb.equal(ugmJoin.get(UserGroupMapEntity.Fields.businessAccountId), businessAccountId)
            )
                    : cb.isNull(ugmJoin.get(UserGroupMapEntity.Fields.businessAccountId));

            Predicate groupRolesCombined = cb.and(
                    cb.or(groupRolePredicates.toArray(new Predicate[0])),
                    domainPredicate,
                    businessPredicate
            );

            // --- Combine direct and group roles ---
            Predicate combined = cb.or(directRolesCombined, groupRolesCombined);

            return exclude ? cb.not(combined) : combined;
        };
    }

    public static Specification<UserEntity> checkFieldNameOrEmailLikeIn(final Collection<String> searchTerms, final boolean exclude, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(searchTerms)) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            
            for (String searchTerm : searchTerms) {
                Predicate namePredicate = cb.like(cb.lower(root.get(UserEntity.Fields.name)), searchTerm.toLowerCase());
                Predicate emailPredicate = cb.like(cb.lower(root.get(UserEntity.Fields.email)), searchTerm.toLowerCase());

                Predicate nameOrEmailPredicate = cb.or(namePredicate, emailPredicate);
                
                if (exclude) {
                    nameOrEmailPredicate = cb.not(nameOrEmailPredicate);
                }
                
                predicates.add(nameOrEmailPredicate);
            }
            
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<UserEntity> checkUserGroupIdIn(
            final Collection<UUID> userGroupIds,
            final boolean exclude,
            final boolean or,
            final UUID domainId,
            final UUID businessAccountId
    ) {

        return (root, query, cb) -> {

            // if no groups or domain is null, return always true
            if (CollectionUtils.isEmpty(userGroupIds) || domainId == null) {
                return cb.conjunction();
            }

            // join with UserGroupMapEntity
            Root<UserGroupMapEntity> ugm = query.from(UserGroupMapEntity.class);

            // join predicate on user id
            Predicate userJoin = cb.equal(
                    root.get(UserEntity.Fields.id),
                    ugm.get(UserGroupMapEntity.Fields.userId)
            );

            // predicate for user group ids
            Predicate groupCondition = ugm
                    .get(UserGroupMapEntity.Fields.userGroupId)
                    .in(userGroupIds);

            // domain predicate (mandatory)
            Predicate domainCondition = cb.equal(
                    ugm.get(UserGroupMapEntity.Fields.domainId),
                    domainId
            );

            // hierarchical businessAccount logic
            Predicate businessCondition;
            if (businessAccountId != null) {
                // include both specific businessAccount and domain-only groups
                businessCondition = cb.or(
                        cb.isNull(ugm.get(UserGroupMapEntity.Fields.businessAccountId)),
                        cb.equal(
                                ugm.get(UserGroupMapEntity.Fields.businessAccountId),
                                businessAccountId
                        )
                );
            } else {
                // only domain-only groups
                businessCondition = cb.isNull(
                        ugm.get(UserGroupMapEntity.Fields.businessAccountId)
                );
            }

            // combine all predicates
            Predicate combined = cb.and(
                    userJoin,
                    groupCondition,
                    domainCondition,
                    businessCondition
            );

            // invert condition if exclude flag is true
            return exclude ? cb.not(combined) : combined;
        };
    }

    public static Specification<UserEntity> checkBusinessAccountId(final UUID businessAccountId) {
        return (root, query, cb) -> {
            if (businessAccountId == null) {
                return cb.conjunction();
            }

            Root<BusinessAccountUserEntity> businessAccountMapRoot = query.from(BusinessAccountUserEntity.class);

            Predicate joinCondition = cb.equal(
                    root.get(UserEntity.Fields.id),
                    businessAccountMapRoot.get(BusinessAccountUserEntity.Fields.userId)
            );

            Predicate accountIdCondition = cb.equal(
                    businessAccountMapRoot.get(BusinessAccountUserEntity.Fields.businessAccountId),
                    businessAccountId
            );

            return cb.and(joinCondition, accountIdCondition);
        };
    }


}
