package org.twins.core.dao.user;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.domain.search.SpaceSearch;

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
            if (query == null || CollectionUtils.isEmpty(spaceRoles)) {
                return cb.conjunction();
            }

            Subquery<SpaceRoleUserEntity> userSubquery = query.subquery(SpaceRoleUserEntity.class);
            Root<SpaceRoleUserEntity> userRoleRoot = userSubquery.from(SpaceRoleUserEntity.class);

            Subquery<SpaceRoleUserGroupEntity> groupSubquery = query.subquery(SpaceRoleUserGroupEntity.class);
            Root<SpaceRoleUserGroupEntity> groupRoleRoot = groupSubquery.from(SpaceRoleUserGroupEntity.class);

            Predicate userMatch = cb.equal(
                    userRoleRoot.get(SpaceRoleUserEntity.Fields.userId),
                    root.get(UserEntity.Fields.id)
            );

            List<Predicate> userRolePredicates = new ArrayList<>();
            List<Predicate> groupRolePredicates = new ArrayList<>();

            for (SpaceSearch spaceRole : spaceRoles) {
                if (spaceRole == null) continue;

                Predicate userSpaceRole = cb.and(
                        spaceRole.getSpaceId() != null ?
                                cb.equal(userRoleRoot.get(SpaceRoleUserEntity.Fields.twinId), spaceRole.getSpaceId()) :
                                cb.conjunction(),
                        spaceRole.getRoleId() != null ?
                                cb.equal(userRoleRoot.get(SpaceRoleUserEntity.Fields.spaceRoleId), spaceRole.getRoleId()) :
                                cb.conjunction()
                );

                Predicate groupSpaceRole = cb.and(
                        spaceRole.getSpaceId() != null ?
                                cb.equal(groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.twinId), spaceRole.getSpaceId()) :
                                cb.conjunction(),
                        spaceRole.getRoleId() != null ?
                                cb.equal(groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.spaceRoleId), spaceRole.getRoleId()) :
                                cb.conjunction()
                );

                userRolePredicates.add(userSpaceRole);
                groupRolePredicates.add(groupSpaceRole);
            }

            if (!userRolePredicates.isEmpty()) {
                userSubquery.select(userRoleRoot)
                        .where(cb.and(userMatch, cb.or(userRolePredicates.toArray(new Predicate[0]))));
            } else {
                userSubquery.where(cb.disjunction());
            }

            Predicate groupConditions = cb.and(
                    cb.or(groupRolePredicates.toArray(new Predicate[0])),
                    cb.equal(
                            cb.function(
                                    "is_user_in_group",
                                    Boolean.class,
                                    root.get(UserEntity.Fields.id),
                                    groupRoleRoot.get(SpaceRoleUserGroupEntity.Fields.userGroupId),
                                    cb.literal(domainId),
                                    cb.literal(businessAccountId)
                            ),
                            true
                    )
            );

            groupSubquery.select(groupRoleRoot).where(groupConditions);

            Predicate combined = cb.or(
                    cb.exists(userSubquery),
                    cb.exists(groupSubquery)
            );

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

    public static <T> Specification<UserEntity> checkUserGroupIn(final Collection<UUID> userGroupIds, final boolean exclude, final boolean or, final Class<T> userGroupMapEntityClass) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(userGroupIds)) {
                return cb.conjunction();
            }

            Root<T> userGroupMapRoot = query.from(userGroupMapEntityClass);

            Predicate joinCondition = cb.equal(
                    root.get(UserEntity.Fields.id),
                    userGroupMapRoot.get("userId")
            );

            List<Predicate> groupPredicates = userGroupIds.stream()
                    .map(groupId -> cb.equal(
                            userGroupMapRoot.get("userGroupId"),
                            groupId
                    ))
                    .toList();

            Predicate groupIdCondition = or
                    ? cb.or(groupPredicates.toArray(new Predicate[0]))
                    : cb.and(groupPredicates.toArray(new Predicate[0]));

            Predicate combinedCondition = cb.and(joinCondition, groupIdCondition);

            return exclude ? cb.not(combinedCondition) : combinedCondition;
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
