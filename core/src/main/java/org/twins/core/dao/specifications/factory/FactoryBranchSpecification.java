package org.twins.core.dao.specifications.factory;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.specifications.CommonSpecification;

@Slf4j
public class FactoryBranchSpecification extends CommonSpecification<TwinFactoryBranchEntity> {


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
