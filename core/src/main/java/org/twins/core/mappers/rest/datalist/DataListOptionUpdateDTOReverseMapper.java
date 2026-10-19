package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.dto.rest.datalist.DataListOptionUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class DataListOptionUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionUpdateRqDTOv1, DataListOptionUpdate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(DataListOptionUpdateRqDTOv1 src, DataListOptionUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setIcon(src.getIcon())
                .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getOptionI18n(), mapperContext))
                .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext))
                .setAttributes(src.getAttributesMap())
                .setExternalId(src.getExternalId())
                .setBackgroundColor(src.getBackgroundColor())
                .setFontColor(src.getFontColor());
        dst.setStatus(src.getStatus());
    }
}
