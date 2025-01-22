package org.twins.core.dao.specifications.factory;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.specifications.CommonSpecification;



@Slf4j
public class FactoryPipelineSpecification extends CommonSpecification<TwinFactoryPipelineEntity> {


    public static Specification<TwinFactoryPipelineEntity> checkTernary(final String field, Ternary required) {
        return (root, query, cb) -> {
            if (required == null)
                return cb.conjunction();
            return switch (required) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }

}
