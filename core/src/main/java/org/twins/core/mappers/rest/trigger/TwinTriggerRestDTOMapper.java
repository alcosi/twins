package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinTriggerMode.class)
public class TwinTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerEntity, TwinTriggerDTOv1> {
    @MapperModePointerBinding(modes = FeaturerMode.TwinTrigger2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinTriggerService twinTriggerService;

    @Override
    public void map(TwinTriggerEntity src, TwinTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTriggerFeaturerId(src.getTwinTriggerFeaturerId())
                    .setTriggerParams(src.getTwinTriggerParam())
                    .setName(src.getName())
                    .setDescription(src.getDescription())
                    .setActive(src.getActive())
                    .setJobTwinClassId(src.getJobTwinClassId());
            case SHORT -> dst
                    .setId(src.getId())
                    .setName(src.getName())
                    .setJobTwinClassId(src.getJobTwinClassId());
        }

        if (mapperContext.hasModeButNot(FeaturerMode.TwinTrigger2FeaturerMode.HIDE)) {
            twinTriggerService.loadTwinTriggerFeaturer(src);
            dst.setTriggerFeaturerId(src.getTwinTriggerFeaturerId());
            featurerRestDTOMapper.postpone(src.getTwinTriggerFeaturer(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinTrigger2FeaturerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.TwinTrigger2TwinClassMode.HIDE)) {
            dst.setJobTwinClassId(src.getJobTwinClassId());
            twinClassRestDTOMapper.postpone(src.getJobTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinTrigger2TwinClassMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FeaturerMode.TwinTrigger2FeaturerMode.HIDE)) {
            twinTriggerService.loadTwinTriggerFeaturer(srcCollection);
        }
    }
}
