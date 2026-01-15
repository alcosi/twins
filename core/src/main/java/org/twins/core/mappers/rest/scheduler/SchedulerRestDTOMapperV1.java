package org.twins.core.mappers.rest.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
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

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SchedulerMode.class)
public class SchedulerRestDTOMapperV1 extends RestSimpleDTOMapper<SchedulerEntity, SchedulerDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.Scheduler2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FeaturerService featurerService;

    @Override
    public void map(SchedulerEntity src, SchedulerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SchedulerMode.SHORT)) {
            case SHORT ->  dst
                    .setId(src.getId());
            case DETAILED ->  dst
                    .setId(src.getId())
                    .setSchedulerFeaturerId(src.getSchedulerFeaturerId())
                    .setSchedulerParams(src.getSchedulerParams())
                    .setActive(src.getActive())
                    .setLogEnabled(src.getLogEnabled())
                    .setCron(src.getCron())
                    .setFixedRate(src.getFixedRate())
                    .setDescription(src.getDescription())
                    .setCreatedAt(src.getCreatedAt())
                    .setUpdatedAt(src.getUpdatedAt());
        }

        if (mapperContext.hasModeButNot(FeaturerMode.Scheduler2FeaturerMode.HIDE)) {
            // load instead of getFeaturer to prevent extra db calls in convertCollection
            featurerService.loadFeaturers(
                    List.of(src),
                    SchedulerEntity::getId,
                    SchedulerEntity::getSchedulerFeaturerId,
                    SchedulerEntity::getSchedulerFeaturer,
                    SchedulerEntity::setSchedulerFeaturer
            );
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

        featurerService.loadFeaturers(
                srcCollection,
                SchedulerEntity::getId,
                SchedulerEntity::getSchedulerFeaturerId,
                SchedulerEntity::getSchedulerFeaturer,
                SchedulerEntity::setSchedulerFeaturer
        );
    }
}
