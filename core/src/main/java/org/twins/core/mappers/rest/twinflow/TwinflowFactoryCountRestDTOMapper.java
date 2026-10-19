package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCountDTOv1;
import org.twins.core.enums.sort.TwinflowFactoryGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowFactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowMode;
import org.twins.core.service.twinflow.TwinflowFactoryService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowFactoryMode.class)
public class TwinflowFactoryCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinflowFactoryEntity, TwinflowFactoryGroupField>, TwinflowFactoryCountDTOv1> {

    @MapperModePointerBinding(modes = TwinflowMode.TwinflowFactory2TwinflowMode.class)
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.TwinflowFactory2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    private final TwinflowFactoryService twinflowFactoryService;

    @Override
    public void map(CountResult<TwinflowFactoryEntity, TwinflowFactoryGroupField> src, TwinflowFactoryCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinflowId(entity.getTwinflowId())
                .setFactoryId(entity.getTwinFactoryId())
                .setTwinFactoryLauncherId(entity.getTwinFactoryLauncher())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinflowMode.TwinflowFactory2TwinflowMode.HIDE, src, TwinflowFactoryGroupField.twinflowId)) {
            twinflowFactoryService.loadTwinflow(entity);
            twinflowBaseV1RestDTOMapper.convertOrPostpone(entity.getTwinflow(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinflowMode.TwinflowFactory2TwinflowMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryMode.TwinflowFactory2FactoryMode.HIDE, src, TwinflowFactoryGroupField.factoryId)) {
            twinflowFactoryService.loadTwinFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.TwinflowFactory2FactoryMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinflowFactoryEntity, TwinflowFactoryGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinflowMode.TwinflowFactory2TwinflowMode.HIDE, sample, TwinflowFactoryGroupField.twinflowId)) {
            twinflowFactoryService.loadTwinflow(entityCollection);
        }
        if (needLoad(mapperContext, FactoryMode.TwinflowFactory2FactoryMode.HIDE, sample, TwinflowFactoryGroupField.factoryId)) {
            twinflowFactoryService.loadTwinFactory(entityCollection);
        }
    }
}
