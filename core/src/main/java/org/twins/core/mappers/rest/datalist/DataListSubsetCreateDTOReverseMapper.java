package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListSubsetCreate;
import org.twins.core.dto.rest.datalist.DataListSubsetCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetCreateDTOv1, DataListSubsetCreate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(DataListSubsetCreateDTOv1 src, DataListSubsetCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setDataListId(src.getDataListId())
                .setKey(src.getKey())
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext));
    }
}
