package org.twins.core.featurer.trigger;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1511,
        name = "CascadeToHeadParent",
        description = "Cascade destination status to head-based parent(s)")
@RequiredArgsConstructor
public class TwinTriggerCascadeToHeadParent extends TwinTrigger {

    @FeaturerParam(name = "Depth", description = "Max cascade depth up the hierarchy (1 = direct parent only, null = unlimited)", optional = true, defaultValue = "1")
    public static final FeaturerParamInt depth = new FeaturerParamInt("depth");

    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinRepository twinRepository;

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Integer depthValue = depth.extract(properties);
        log.info("Running CascadeToHeadParent: twin {}, destination status '{}', depth: {}", twinEntity.logShort(), dstTwinStatus.getKey(), depthValue);

        // Build specification to find all ancestors up to specified depth using ltree
        Specification<TwinEntity> spec = (Root<TwinEntity> root, jakarta.persistence.criteria.CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (twinEntity.getId() == null) {
                return cb.disjunction();
            }

            Integer preparedDepthLimit = depthValue == null || depthValue <= 0 ? Integer.MAX_VALUE : depthValue;

            // Create subquery to find parents using ltree function
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TwinEntity> subqueryRoot = subquery.from(TwinEntity.class);

            subquery.select(
                cb.function("ltree_of_uuids_get_parents", UUID.class,
                    subqueryRoot.get(TwinEntity.Fields.hierarchyTree),
                    cb.literal(preparedDepthLimit)
                )
            );

            subquery.where(subqueryRoot.get(TwinEntity.Fields.id).in(Collections.singleton(twinEntity.getId())));

            // Main query: find twins whose id is in the parent subquery result
            return root.get(TwinEntity.Fields.id).in(subquery);
        };

        List<TwinEntity> parents = twinRepository.findAll(spec);

        if (!parents.isEmpty()) {
            twinService.changeStatus(parents, dstTwinStatus);
        }
    }
}
