package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListCreate;
import org.twins.core.dto.rest.datalist.DataListCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListCreateRqDTOv1, DataListCreate> {

    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final DataListAttributeRestDTOReverseMapper dataListAttributeRestDTOReverseMapper;

    @Override
    public void map(DataListCreateRqDTOv1 src, DataListCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
                .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext))
                .setAttribute1(dataListAttributeRestDTOReverseMapper.convert(src.getAttribute1()))
                .setAttribute2(dataListAttributeRestDTOReverseMapper.convert(src.getAttribute2()))
                .setAttribute3(dataListAttributeRestDTOReverseMapper.convert(src.getAttribute3()))
                .setAttribute4(dataListAttributeRestDTOReverseMapper.convert(src.getAttribute4()))
                .setExternalId(src.getExternalId());
    }
}
