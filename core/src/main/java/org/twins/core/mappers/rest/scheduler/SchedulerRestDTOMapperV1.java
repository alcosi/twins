package org.twins.core.mappers.rest.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dto.rest.scheduler.SchedulerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.SchedulerMode;
import org.twins.core.service.scheduler.SchedulerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SchedulerMode.class)
public class SchedulerRestDTOMapperV1 extends RestSimpleDTOMapper<SchedulerEntity, SchedulerDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.Scheduler2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final SchedulerService schedulerService;

    @Override
    public void map(SchedulerEntity src, SchedulerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SchedulerMode.SHORT)) {
            case SHORT ->  dst
                    .setId(src.getId())
                    .setSchedulerFeaturerId(src.getSchedulerFeaturerId())
                    .setDescription(src.getDescription());
            case DETAILED ->  dst
                    .setId(src.getId())
                    .setSchedulerFeaturerId(src.getSchedulerFeaturerId())
                    .setSchedulerParams(src.getSchedulerParams())
                    .setActive(src.getActive())
                    .setLogEnabled(src.getLogEnabled())
                    .setCron(src.getCron())
                    .setFixedRate(src.getFixedRate())
                    .setDescription(src.getDescription())
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                    .setUpdatedAt(src.getUpdatedAt().toLocalDateTime());
        }

        if (mapperContext.hasModeButNot(FeaturerMode.Scheduler2FeaturerMode.HIDE)) {
            schedulerService.loadFeaturer(src);
            dst.setSchedulerFeaturerId(src.getSchedulerFeaturerId());
            featurerRestDTOMapper.postpone(src.getSchedulerFeaturer(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.Scheduler2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<SchedulerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        if (srcCollection.isEmpty()) {
            return;
        }

        if (mapperContext.hasModeButNot(FeaturerMode.Scheduler2FeaturerMode.HIDE)) {
            schedulerService.loadFeaturers(srcCollection);
        }
    }
}
