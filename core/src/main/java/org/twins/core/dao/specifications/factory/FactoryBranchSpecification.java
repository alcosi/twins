package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class FactoryBranchSpecification extends CommonSpecification<TwinFactoryBranchEntity> {

    public static Specification<TwinFactoryBranchEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            var factoryPredicate = createPredicateWithJoins(
                    root, cb, domainId,
                    (property, criteriaBuilder, filedValue) -> criteriaBuilder.equal(property, filedValue),
                    JoinType.INNER, TwinFactoryBranchEntity.Fields.factory, TwinFactoryEntity.Fields.domainId
            );
            var nextFactoryPredicate = createPredicateWithJoins(
                    root, cb, domainId,
                    (property, criteriaBuilder, filedValue) -> criteriaBuilder.equal(property, filedValue),
                    JoinType.INNER, TwinFactoryBranchEntity.Fields.nextFactory, TwinFactoryEntity.Fields.domainId
            );
            return cb.and(factoryPredicate, nextFactoryPredicate);
        };
    }

    public static Specification<TwinFactoryBranchEntity> checkTernary(Ternary ternary, final String field) {
        return (root, query, cb) -> {
            if (ternary == null)
                return cb.conjunction();
            return switch (ternary) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }

}
