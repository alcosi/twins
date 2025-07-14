package org.twins.core.mappers.rest.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.dto.rest.statistic.TwinStatisticProgressPercentDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class StatisticProgressPercentRestDTOMapper extends RestSimpleDTOMapper<TwinStatisticProgressPercent, TwinStatisticProgressPercentDTOv1> {
    private final ItemDTOMapper itemDTOMapper;

    @Override
    public void map(TwinStatisticProgressPercent src, TwinStatisticProgressPercentDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setItems(itemDTOMapper.convertCollection(src.getItems(), mapperContext));
    }
}
