package org.twins.core.mappers.rest.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dto.rest.scheduler.SchedulerLogDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.SchedulerLogMode;
import org.twins.core.mappers.rest.mappercontext.modes.SchedulerMode;
import org.twins.core.service.scheduler.SchedulerLogService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class SchedulerLogRestDTOMapperV1 extends RestSimpleDTOMapper<SchedulerLogEntity, SchedulerLogDTOv1> {

    @MapperModePointerBinding(modes = SchedulerMode.SchedulerLog2SchedulerMode.class)
    private final SchedulerRestDTOMapperV1 schedulerRestDTOMapperV1;
    private final SchedulerLogService schedulerLogService;

    @Override
    public void map(SchedulerLogEntity src, SchedulerLogDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SchedulerLogMode.SHORT)) {
            case SHORT -> dst
                    .setId(src.getId())
                    .setSchedulerId(src.getSchedulerId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setSchedulerId(src.getSchedulerId())
                    .setResult(src.getResult())
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                    .setExecutionTime(src.getExecutionTime());
        }

        if (mapperContext.hasModeButNot(SchedulerMode.SchedulerLog2SchedulerMode.HIDE)) {
            schedulerLogService.loadScheduler(src);
            dst.setId(src.getId());
            schedulerRestDTOMapperV1.postpone(src.getScheduler(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(SchedulerMode.SchedulerLog2SchedulerMode.DETAILED)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<SchedulerLogEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        if (srcCollection.isEmpty()) {
            return;
        }

        if (mapperContext.hasModeButNot(SchedulerMode.SchedulerLog2SchedulerMode.HIDE)) {
            schedulerLogService.loadSchedulers(srcCollection);
        }
    }
}
