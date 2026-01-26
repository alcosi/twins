package org.twins.core.mappers.rest.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.dto.rest.statistic.TwinStatisticProgressPercentItemDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;


@Component
@RequiredArgsConstructor
public class ItemDTOMapper extends RestSimpleDTOMapper<TwinStatisticProgressPercent.Item, TwinStatisticProgressPercentItemDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(TwinStatisticProgressPercent.Item src, TwinStatisticProgressPercentItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()))
                .setKey(src.getKey())
                .setPercent(src.getPercent())
                .setColorHex(src.getColorHex());
    }
}
