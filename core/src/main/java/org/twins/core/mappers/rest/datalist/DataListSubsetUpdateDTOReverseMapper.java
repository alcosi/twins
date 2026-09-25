package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListSubsetUpdate;
import org.twins.core.dto.rest.datalist.DataListSubsetUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetUpdateDTOv1, DataListSubsetUpdate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(DataListSubsetUpdateDTOv1 src, DataListSubsetUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext));
    }
}
