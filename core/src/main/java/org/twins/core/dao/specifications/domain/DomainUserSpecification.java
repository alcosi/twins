package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserStatus;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

import java.util.*;

public class DomainUserSpecification {

    public static Specification<DomainUserEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ?
                    (ifNotIsTrueIncludeNullValues ? cb.or(root.get(uuidField).in(uuids).not(), root.get(uuidField).isNull()) : root.get(uuidField).in(uuids).not())
                    : root.get(uuidField).in(uuids);
        };
    }

    public static Specification<DomainUserEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            Join<DomainUserEntity, UserEntity> userJoin = root.join(DomainUserEntity.Fields.user, JoinType.INNER);
            if (search != null && !search.isEmpty()) {
                for (String name : search) {
                    Predicate predicate = cb.like(cb.lower(userJoin.get(field)), "%" + name.toLowerCase() + "%");
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DomainUserEntity> checkFieldNotLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<DomainUserEntity, UserEntity> userJoin = root.join(DomainUserEntity.Fields.user, JoinType.INNER);
            if (search != null && !search.isEmpty()) {
                for (String name : search) {
                    Predicate predicate = cb.not(cb.like(cb.lower(userJoin.get(field)), "%" + name.toLowerCase() + "%"));
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DomainUserEntity> checkUserStatusIn(final Set<UserStatus> statuses, final boolean not) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();

            Join<DomainUserEntity, UserEntity> userJoin = root.join(DomainUserEntity.Fields.user, JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            for (UserStatus status : statuses) {
                Predicate predicate = cb.equal(userJoin.get(UserEntity.Fields.userStatusId), status);
                predicates.add(not ? predicate.not() : predicate);
            }
            return getPredicate(cb, predicates, false);
        };
    }
}
