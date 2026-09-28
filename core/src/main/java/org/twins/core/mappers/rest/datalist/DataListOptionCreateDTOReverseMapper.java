package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.dto.rest.datalist.DataListOptionCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class DataListOptionCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionCreateRqDTOv1, DataListOptionCreate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(DataListOptionCreateRqDTOv1 src, DataListOptionCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setDataListId(src.getDataListId())
                .setIcon(src.getIcon())
                .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getOptionI18n(), mapperContext))
                .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext))
                .setAttributes(src.getAttributesMap())
                .setExternalId(src.getExternalId())
                .setBackgroundColor(src.getBackgroundColor())
                .setFontColor(src.getFontColor());
    }
}
