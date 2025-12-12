package org.twins.core.mappers.rest.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SchedulerSearch;
import org.twins.core.dto.rest.scheduler.SchedulerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SchedulerSearchRestDTOReverseMapper extends RestSimpleDTOMapper<SchedulerSearchDTOv1, SchedulerSearch> {

    @Override
    public void map(SchedulerSearchDTOv1 src, SchedulerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdSet(src.getIdSet())
                .setIdExcludeSet(src.getIdExcludeSet())
                .setFeaturerIdSet(src.getFeaturerIdSet())
                .setFeaturerIdExcludeSet(src.getFeaturerIdExcludeSet())
                .setCronSet(src.getCronSet())
                .setCronExcludeSet(src.getCronExcludeSet())
                .setFixedRateRange(src.getFixedRateRange())
                .setActive(src.getActive())
                .setLogEnabled(src.getLogEnabled())
                .setDescriptionSet(src.getDescriptionSet())
                .setDescriptionExcludeSet(src.getDescriptionExcludeSet())
                .setCreatedAtRange(src.getCreatedAtRange())
                .setUpdatedAtRange(src.getUpdatedAtRange());
    }
}
