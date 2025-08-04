package org.twins.core.mappers.rest.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.dto.rest.statistic.TwinStatisticProgressPercentDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class StatisticRestDTOMapper extends RestSimpleDTOMapper<Map<UUID, TwinStatisticProgressPercent>, Map<UUID, TwinStatisticProgressPercentDTOv1>> {
    private final StatisticProgressPercentRestDTOMapper statisticProgressPercentRestDTOMapper;

    @Override
    public void map(Map<UUID, TwinStatisticProgressPercent> src, Map<UUID, TwinStatisticProgressPercentDTOv1> dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    public Map<UUID, TwinStatisticProgressPercentDTOv1> convert(Map<UUID, TwinStatisticProgressPercent> src, MapperContext mapperContext) throws Exception {
        if (CollectionUtils.isEmpty(src))
            return null;
        Map<UUID, TwinStatisticProgressPercentDTOv1> dst = new HashMap<>();
        for (var map : src.entrySet()) {
            dst.put(map.getKey(), statisticProgressPercentRestDTOMapper.convert(map.getValue()));
        }
        return dst;
    }
}
