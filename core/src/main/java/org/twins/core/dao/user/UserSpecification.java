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
}
