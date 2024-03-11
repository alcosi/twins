package org.twins.core.dao.specifications.space;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;

import java.util.*;

@Slf4j
public class SpaceRoleUserSpecification {

    public static Specification<SpaceRoleUserEntity> checkUuid(final String uuidField, final UUID uuid, boolean not) {
        return (root, query, cb) -> not ? cb.equal(root.get(uuidField), uuid).not() : cb.equal(root.get(uuidField), uuid);
    }

    public static Specification<SpaceRoleUserEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(uuidField).in(uuids).not() : root.get(uuidField).in(uuids);
        };
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
            if (CollectionUtils.isEmpty(groupIds)) return cb.conjunction();
            Join<SpaceRoleUserEntity, UserEntity> userJoin = root.join("user");
            Join<UserEntity, UserGroupMapEntity> groupMapJoin = userJoin.join("userGroupMaps");
            Join<UserGroupMapEntity, UserGroupEntity> groupJoin = groupMapJoin.join("userGroup");
            Predicate groupCondition = groupJoin.get("id").in(groupIds);
            if (not) return cb.not(groupCondition);
            else return groupCondition;
        };
    }

    public static Specification<SpaceRoleUserEntity> checkUserNameLikeWithPattern(final String search) {
        return (root, query, cb) -> {
            if (!ObjectUtils.isEmpty(search))
                return cb.like(cb.lower(root.join(SpaceRoleUserEntity.Fields.user).get(UserEntity.Fields.name)), "%" + search.toLowerCase() + "%");
            else return cb.conjunction();
        };
    }

    public static Specification<SpaceRoleUserEntity> checkUserNameLikeWithoutPattern(final String search) {
        return (root, query, cb) -> {
            if (!ObjectUtils.isEmpty(search))
                return cb.like(cb.lower(root.join(SpaceRoleUserEntity.Fields.user).get(UserEntity.Fields.name)), search.toLowerCase());
            else return cb.conjunction();
        };
    }


}
