package org.twins.core.dao.user;

import jakarta.persistence.criteria.*;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
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

    public static Specification<UserEntity> checkSpaceRoleLikeIn(List<SpaceSearchDTOv1> spaceRoles, boolean exclude) {
        return (root, query, cb) -> {
            if (query == null || CollectionUtils.isEmpty(spaceRoles)) {
                return cb.conjunction();
            }

            Subquery<SpaceRoleUserEntity> subquery = query.subquery(SpaceRoleUserEntity.class);
            Root<SpaceRoleUserEntity> spaceRoleRoot = subquery.from(SpaceRoleUserEntity.class);

            Predicate userMatch = cb.equal(
                    spaceRoleRoot.get(SpaceRoleUserEntity.Fields.userId),
                    root.get(UserEntity.Fields.id)
            );

            Predicate[] rolePredicates = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .map(spaceRole -> {
                        Predicate conditions = cb.conjunction();

                        if (spaceRole.getSpaceId() != null) {
                            conditions = cb.and(
                                    conditions,
                                    cb.equal(
                                            spaceRoleRoot.get(SpaceRoleUserEntity.Fields.twinId),
                                            spaceRole.getSpaceId()
                                    )
                            );
                        }

                        if (spaceRole.getRoleId() != null) {
                            conditions = cb.and(
                                    conditions,
                                    cb.equal(
                                            spaceRoleRoot.get(SpaceRoleUserEntity.Fields.spaceRoleId),
                                            spaceRole.getRoleId()
                                    )
                            );
                        }

                        return conditions;
                    })
                    .filter(p -> !p.equals(cb.conjunction()))
                    .toArray(Predicate[]::new);

            if (rolePredicates.length == 0) {
                return cb.conjunction();
            }

            subquery.select(spaceRoleRoot)
                    .where(cb.and(userMatch, cb.or(rolePredicates)));

            return exclude
                    ? cb.not(cb.exists(subquery))
                    : cb.exists(subquery);
        };
    }

    public static Specification<UserEntity> checkSpaceRoleGroupLikeIn(List<SpaceSearchDTOv1> spaceRoles, boolean exclude) {
        return (root, query, cb) -> {
            if (query == null || CollectionUtils.isEmpty(spaceRoles)) {
                return cb.conjunction();
            }

            Subquery<SpaceRoleUserGroupEntity> groupSubquery = query.subquery(SpaceRoleUserGroupEntity.class);
            Root<SpaceRoleUserGroupEntity> groupRoot = groupSubquery.from(SpaceRoleUserGroupEntity.class);

            Predicate groupMembershipCondition = cb.or(
                    createUserGroupMappingCondition(query, cb, root, groupRoot, UserGroupMapType1Entity.class),
                    createUserGroupMappingCondition(query, cb, root, groupRoot, UserGroupMapType2Entity.class),
                    createUserGroupMappingCondition(query, cb, root, groupRoot, UserGroupMapType3Entity.class)
            );

            List<Predicate> roleGroupPredicates = spaceRoles.stream()
                    .filter(Objects::nonNull)
                    .map(spaceRole -> buildSpaceRoleGroupPredicate(cb, groupRoot, spaceRole))
                    .filter(p -> !p.equals(cb.conjunction()))
                    .toList();

            if (roleGroupPredicates.isEmpty()) {
                return cb.conjunction();
            }

            groupSubquery.select(groupRoot)
                    .where(cb.and(
                            cb.or(roleGroupPredicates.toArray(new Predicate[0])),
                            groupMembershipCondition
                    ));

            return exclude
                    ? cb.not(cb.exists(groupSubquery))
                    : cb.exists(groupSubquery);
        };
    }

    private static Predicate buildSpaceRoleGroupPredicate(
            CriteriaBuilder cb,
            Root<SpaceRoleUserGroupEntity> root,
            SpaceSearchDTOv1 spaceRole
    ) {
        Predicate conditions = cb.conjunction();

        if (spaceRole.getSpaceId() != null) {
            conditions = cb.and(
                    conditions,
                    cb.equal(
                            root.get(SpaceRoleUserGroupEntity.Fields.twinId),
                            spaceRole.getSpaceId()
                    )
            );
        }

        if (spaceRole.getRoleId() != null) {
            conditions = cb.and(
                    conditions,
                    cb.equal(
                            root.get(SpaceRoleUserGroupEntity.Fields.spaceRoleId),
                            spaceRole.getRoleId()
                    )
            );
        }

        return conditions;
    }

    private static <T extends UserGroupMap> Predicate createUserGroupMappingCondition(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Root<UserEntity> userRoot,
            Root<SpaceRoleUserGroupEntity> groupRoot,
            Class<T> mapType
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<T> mapRoot = subquery.from(mapType);

        subquery.select(cb.literal(1L))
                .where(cb.and(
                        cb.equal(
                                mapRoot.get("userGroupId"),
                                groupRoot.get(SpaceRoleUserGroupEntity.Fields.userGroupId)
                        ),
                        cb.equal(
                                mapRoot.get("userId"),
                                userRoot.get(UserEntity.Fields.id)
                        )
                ));

        return cb.exists(subquery);
    }
}
