package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.trigger.TwinTriggerCountDTOv1;
import org.twins.core.enums.sort.TwinTriggerGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.twintrigger.TwinTriggerService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinTriggerMode.class)
public class TwinTriggerCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinTriggerEntity, TwinTriggerGroupField>, TwinTriggerCountDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.TwinTrigger2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinTriggerService twinTriggerService;

    @Override
    public void map(CountResult<TwinTriggerEntity, TwinTriggerGroupField> src, TwinTriggerCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTriggerFeaturerId(entity.getTwinTriggerFeaturerId())
                .setActive(entity.getActive())
                .setJobTwinClassId(entity.getJobTwinClassId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FeaturerMode.TwinTrigger2FeaturerMode.HIDE, src, TwinTriggerGroupField.triggerFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getTwinTriggerFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinTrigger2FeaturerMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.TwinTrigger2TwinClassMode.HIDE, src, TwinTriggerGroupField.jobTwinClassId)) {
            twinTriggerService.loadJobTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getJobTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinTrigger2TwinClassMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinTriggerEntity, TwinTriggerGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        // Featurer is postponed per-item by Integer id in map() (no entity batch-load needed — related resolver handles it).
        if (needLoad(mapperContext, TwinClassMode.TwinTrigger2TwinClassMode.HIDE, sample, TwinTriggerGroupField.jobTwinClassId)) {
            twinTriggerService.loadJobTwinClass(entityCollection);
        }
    }
}
