package org.twins.core.mappers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.cambium.common.math.LongRange;
import org.twins.core.dto.rest.LongRangeDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class LongRangeDTOReverseMapper extends RestSimpleDTOMapper<LongRangeDTOv1, LongRange> {
    @Override
    public void map(LongRangeDTOv1 src, LongRange dst, MapperContext mapperContext) throws Exception {
        dst
                .setFrom(src.getFrom())
                .setTo(src.getTo())
        ;
    }
}
