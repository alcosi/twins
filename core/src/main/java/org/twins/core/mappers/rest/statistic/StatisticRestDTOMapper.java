package org.twins.core.mappers.rest.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.dto.rest.statistic.TwinStatisticProgressPercentDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class StatisticRestDTOMapper extends RestSimpleDTOMapper<Map<UUID, TwinStatisticProgressPercent>, TwinStatisticProgressPercentDTOv1> {
    @Override
    public void map(Map<UUID, TwinStatisticProgressPercent> src, TwinStatisticProgressPercentDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinStatistics(src);
    }
}
