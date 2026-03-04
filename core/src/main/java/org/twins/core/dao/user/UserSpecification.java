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

            List<SpaceSearch> validSpaceRoles = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (validSpaceRoles.isEmpty()) {
                return cb.conjunction();
            }

            if (exclude) {
                List<Predicate> excludePredicates = new ArrayList<>();
                for (SpaceSearch spaceRole : validSpaceRoles) {
                    Predicate notDirect = cb.not(existsDirectSpaceRole(root, query, cb, spaceRole));
                    Predicate notViaGroup = cb.not(existsSpaceRoleViaGroup(root, query, cb, spaceRole, domainId, businessAccountId));
                    excludePredicates.add(cb.and(notDirect, notViaGroup));
                }
                return cb.and(excludePredicates.toArray(new Predicate[0]));
            } else {
                // Включаем только пользователей, у которых есть хотя бы одна из ролей (напрямую или через группу).
                List<Predicate> includePredicates = new ArrayList<>();
                for (SpaceSearch spaceRole : validSpaceRoles) {
                    Predicate direct = existsDirectSpaceRole(root, query, cb, spaceRole);
                    Predicate viaGroup = existsSpaceRoleViaGroup(root, query, cb, spaceRole, domainId, businessAccountId);
                    includePredicates.add(cb.or(direct, viaGroup));
                }
                return cb.or(includePredicates.toArray(new Predicate[0]));
            }
        };
    }

    private static Predicate existsDirectSpaceRole(
            Root<UserEntity> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            SpaceSearch spaceRole
    ) {
        Subquery<UUID> sub = query.subquery(UUID.class);
        Root<SpaceRoleUserEntity> sru = sub.from(SpaceRoleUserEntity.class);
        sub.select(sru.get(SpaceRoleUserEntity.Fields.id));
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(sru.get(SpaceRoleUserEntity.Fields.userId), root.get(UserEntity.Fields.id)));
        if (spaceRole.getSpaceId() != null) {
            conditions.add(cb.equal(sru.get(SpaceRoleUserEntity.Fields.twinId), spaceRole.getSpaceId()));
        }
        if (spaceRole.getRoleId() != null) {
            conditions.add(cb.equal(sru.get(SpaceRoleUserEntity.Fields.spaceRoleId), spaceRole.getRoleId()));
        }
        sub.where(cb.and(conditions.toArray(new Predicate[0])));
        return cb.exists(sub);
    }

    private static Predicate existsSpaceRoleViaGroup(
            Root<UserEntity> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            SpaceSearch spaceRole,
            UUID domainId,
            UUID businessAccountId
    ) {
        Subquery<UUID> sub = query.subquery(UUID.class);
        Root<UserGroupMapEntity> ugm = sub.from(UserGroupMapEntity.class);
        Join<UserGroupMapEntity, UserGroupEntity> ug = ugm.join(UserGroupMapEntity.Fields.userGroup, JoinType.INNER);
        Join<UserGroupEntity, SpaceRoleUserGroupEntity> srug = ug.join(UserGroupEntity.Fields.spaceRoleUserGroups, JoinType.INNER);
        sub.select(ugm.get(UserGroupMapEntity.Fields.id));
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(ugm.get(UserGroupMapEntity.Fields.userId), root.get(UserEntity.Fields.id)));
        if (domainId != null) {
            conditions.add(cb.equal(ugm.get(UserGroupMapEntity.Fields.domainId), domainId));
        } else {
            conditions.add(cb.isNull(ugm.get(UserGroupMapEntity.Fields.domainId)));
        }
        if (businessAccountId != null) {
            conditions.add(cb.or(
                    cb.isNull(ugm.get(UserGroupMapEntity.Fields.businessAccountId)),
                    cb.equal(ugm.get(UserGroupMapEntity.Fields.businessAccountId), businessAccountId)
            ));
        } else {
            conditions.add(cb.isNull(ugm.get(UserGroupMapEntity.Fields.businessAccountId)));
        }
        if (spaceRole.getSpaceId() != null) {
            conditions.add(cb.equal(srug.get(SpaceRoleUserGroupEntity.Fields.twinId), spaceRole.getSpaceId()));
        }
        if (spaceRole.getRoleId() != null) {
            conditions.add(cb.equal(srug.get(SpaceRoleUserGroupEntity.Fields.spaceRoleId), spaceRole.getRoleId()));
        }
        sub.where(cb.and(conditions.toArray(new Predicate[0])));
        return cb.exists(sub);
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
