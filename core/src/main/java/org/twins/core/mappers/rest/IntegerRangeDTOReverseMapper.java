package org.twins.core.mappers.rest;

import lombok.RequiredArgsConstructor;
import org.cambium.common.math.IntegerRange;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.IntegerRangeDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class IntegerRangeDTOReverseMapper extends RestSimpleDTOMapper<IntegerRangeDTOv1, IntegerRange> {

    @Override
    public void map(IntegerRangeDTOv1 src, IntegerRange dst, MapperContext mapperContext) throws Exception {
        dst
                .setFrom(src.getFrom())
                .setTo(src.getTo())
        ;
    }
}
