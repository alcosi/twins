package org.twins.core.mappers.rest.statistic;

import org.springframework.stereotype.Component;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.dto.rest.statistic.TwinStatisticProgressPercentItemDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class ItemDTOMapper extends RestSimpleDTOMapper<TwinStatisticProgressPercent.Item, TwinStatisticProgressPercentItemDTOv1> {
    @Override
    public void map(TwinStatisticProgressPercent.Item src, TwinStatisticProgressPercentItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setLabel(src.getLabel())
                .setKey(src.getKey())
                .setPercent(src.getPercent())
                .setColorHex(src.getColorHex());
    }
}
