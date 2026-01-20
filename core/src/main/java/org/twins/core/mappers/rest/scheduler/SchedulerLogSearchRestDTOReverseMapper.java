package org.twins.core.mappers.rest.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SchedulerLogSearch;
import org.twins.core.dto.rest.scheduler.SchedulerLogSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SchedulerLogSearchRestDTOReverseMapper extends RestSimpleDTOMapper<SchedulerLogSearchDTOv1, SchedulerLogSearch> {

    private final DataTimeRangeDTOReverseMapper dateMapper;
    private final LongRangeDTOReverseMapper longRangeMapper;

    @Override
    public void map(SchedulerLogSearchDTOv1 src, SchedulerLogSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdSet(src.getIdSet())
                .setIdExcludeSet(src.getIdExcludeSet())
                .setSchedulerIdSet(src.getSchedulerIdSet())
                .setSchedulerIdExcludeSet(src.getSchedulerIdExcludeSet())
                .setResultLikeSet(src.getResultLikeSet())
                .setResultNotLikeSet(src.getResultNotLikeSet())
                .setCreatedAt(dateMapper.convert(src.getCreatedAt()))
                .setExecutionTimeRange(longRangeMapper.convert(src.getExecutionTimeRange()));
    }
}
