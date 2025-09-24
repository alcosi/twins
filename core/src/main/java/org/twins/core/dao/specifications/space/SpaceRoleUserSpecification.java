package org.twins.core.dao.specifications.space;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.user.UserEntity;

import java.util.Collection;
import java.util.UUID;

@Slf4j
public class SpaceRoleUserSpecification extends CommonSpecification<SpaceRoleUserEntity> {

    public static Specification<SpaceRoleUserEntity> checkUuid(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> not ? cb.not(cb.equal(root.get(uuidField), uuid)) : cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<SpaceRoleUserEntity> checkUserInSpaceGroups(final Collection<UUID> groupIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(groupIds)) return cb.conjunction();
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<SpaceRoleUserGroupEntity> subRoot = subquery.from(SpaceRoleUserGroupEntity.class);
            subquery.select(subRoot.get(SpaceRoleUserGroupEntity.Fields.twinId)).where(subRoot.get(SpaceRoleUserGroupEntity.Fields.userGroupId).in(groupIds));
            if (not) return cb.not(root.get(SpaceRoleUserEntity.Fields.twinId).in(subquery));
            else return root.get(SpaceRoleUserEntity.Fields.twinId).in(subquery);
        };
    }

    public static Specification<SpaceRoleUserEntity> checkUserInGroups(final Collection<UUID> groupIds, boolean not) {
        return (root, query, cb) -> {
            return cb.conjunction();
            //todo
//            if (CollectionUtils.isEmpty(groupIds)) return cb.conjunction();
//            Subquery<UUID> subquery = query.subquery(UUID.class);
//            Root<UserGroupMapEntity> subqueryRoot = subquery.from(UserGroupMapEntity.class);
//            Join<UserGroupMapEntity, UserGroupEntity> subqueryGroupJoin = subqueryRoot.join(UserGroupMapEntity.Fields.userGroup);
//            subquery.select(subqueryRoot.get(UserGroupMapEntity.Fields.user).get(UserEntity.Fields.id));
//            Predicate groupIdInList = subqueryGroupJoin.get(UserGroupEntity.Fields.id).in(groupIds);
//            subquery.where(groupIdInList);
//            Join<SpaceRoleUserEntity, UserEntity> userJoin = root.join(SpaceRoleUserEntity.Fields.user);
//            Predicate userInGroupPredicate = cb.in(userJoin.get(UserEntity.Fields.id)).value(subquery);
//            if (not) return cb.not(userInGroupPredicate);
//            else return userInGroupPredicate;
        };
    }

    public static Specification<SpaceRoleUserEntity> checkUserNameLikeWithPattern(final String search) {
        return (root, query, cb) -> {
            if (!ObjectUtils.isEmpty(search))
                return cb.like(cb.lower(root.join(SpaceRoleUserEntity.Fields.user).get(UserEntity.Fields.name)), "%" + search.toLowerCase() + "%", escapeChar);
            else return cb.conjunction();
        };
    }

    public static Specification<SpaceRoleUserEntity> checkUserNameLikeWithoutPattern(final String search) {
        return (root, query, cb) -> {
            if (!ObjectUtils.isEmpty(search))
                return cb.like(cb.lower(root.join(SpaceRoleUserEntity.Fields.user).get(UserEntity.Fields.name)), search.toLowerCase(), escapeChar);
            else return cb.conjunction();
        };
    }


}
