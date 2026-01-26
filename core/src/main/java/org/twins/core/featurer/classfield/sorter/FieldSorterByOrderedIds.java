package org.twins.core.featurer.classfield.sorter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4002,
        name = "By ordered ids",
        description = "")
public class FieldSorterByOrderedIds extends FieldSorter {
    @FeaturerParam(name = "Field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    public Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> createSort(Properties properties) throws ServiceException {
        Set<UUID> orderedIds = fieldIds.extract(properties);
        return baseSpec -> (root, query, cb) -> {
            Predicate basePredicate = baseSpec == null ? null : baseSpec.toPredicate(root, query, cb);

            if (!query.getResultType().equals(Long.class)) {
                CriteriaBuilder.Case<Integer> orderCase = cb.selectCase();

                int index = 0;
                for (UUID uuid : orderedIds) {
                    orderCase = orderCase.when(cb.equal(root.get("id"), uuid), index++);
                }

                query.orderBy(cb.asc(orderCase));
            }
            return basePredicate;
        };
    }
}
