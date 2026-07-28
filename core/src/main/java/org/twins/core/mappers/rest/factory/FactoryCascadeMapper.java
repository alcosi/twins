package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryCascadeMode;
import org.twins.core.service.factory.FactoryService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wraps {@link FactoryRestDTOMapper}: when {@link FactoryCascadeMode#SHOW} is active, walks the
 * factory cascade (cycle-safe BFS via {@link FactoryService#expandFactoryCascade}), pre-warms
 * counts/createdByUser in batch (the converter converts per-entity, so batch loaders in
 * beforeCollectionConversion would not fire), and postpones every child factory into
 * {@code relatedFactoryMap}. The standard converter then unfolds the whole graph by id
 * (factory -> pipelineIdList -> factoryPipelineMap -> nextFactoryId -> factoryMap -> ...),
 * so the depth-3 limit of the converter does not truncate the cascade — it is already
 * fully gathered here.
 */
@Component
@RequiredArgsConstructor
public class FactoryCascadeMapper {
    private final FactoryService factoryService;
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    public FactoryDTOv1 convert(TwinFactoryEntity root, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(FactoryCascadeMode.HIDE)) {
            List<TwinFactoryEntity> cascade = factoryService.expandFactoryCascade(
                    List.of(root), FactoryService.FACTORY_CASCADE_HARD_CAP);
            factoryService.countFactoryUsages(cascade);
            factoryService.countFactoryPipelines(cascade);
            factoryService.countFactoryMultipliers(cascade);
            factoryService.countFactoryBranches(cascade);
            factoryService.countFactoryErasers(cascade);
            factoryService.loadCreatedByUser(cascade);
            Map<UUID, RelatedObject<TwinFactoryEntity>> factoryMap = mapperContext.getRelatedFactoryMap();
            for (TwinFactoryEntity factory : cascade) {
                if (!factory.getId().equals(root.getId())) {
                    mapperContext.smartPut(factoryMap, factory, factory.getId());
                }
            }
        }
        return factoryRestDTOMapper.convert(root, mapperContext);
    }
}
