package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinTriggerMode.class)
public class TwinTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerEntity, TwinTriggerDTOv1> {
    @MapperModePointerBinding(modes = TwinTriggerMode.TwinTrigger2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

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
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setName(src.getName());
        }

        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinTrigger2FeaturerMode.HIDE)) {
            featurerRestDTOMapper.postpone(src.getTwinTriggerFeaturer(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinTrigger2FeaturerMode.SHORT)));
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
        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinTrigger2FeaturerMode.HIDE)) {
            twinTriggerService.loadTwinTriggerFeaturer(srcCollection);
        }
    }
}
