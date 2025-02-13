package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionSave;
import org.twins.core.dto.rest.datalist.DataListOptionSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionSaveDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionSaveRqDTOv1, DataListOptionSave> {
    private final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;

    @Override
    public void map(DataListOptionSaveRqDTOv1 src, DataListOptionSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setIcon(src.getIcon())
                .setNameI18n(i18nRestDTOReverseMapper.convert(src.getOptionI18n(), mapperContext))
                .setAttributes(src.getAttributesMap());
    }
}
