package org.twins.core.mappers.rest;

import org.springframework.stereotype.Component;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
public class DataTimeRangeDTOReverseMapper extends RestSimpleDTOMapper<DataTimeRangeDTOv1, DataTimeRange> {
    @Override
    public void map(DataTimeRangeDTOv1 src, DataTimeRange dst, MapperContext mapperContext) {
        dst
                .setFrom(convertOrNull(src.getFrom()))
                .setTo(convertOrNull(src.getTo()));
    }
}
