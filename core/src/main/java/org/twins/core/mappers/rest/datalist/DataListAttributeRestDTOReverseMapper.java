package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListAttribute;
import org.twins.core.dto.rest.datalist.DataListAttributeSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class DataListAttributeRestDTOReverseMapper extends RestSimpleDTOMapper<DataListAttributeSaveDTOv1, DataListAttribute> {

    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(DataListAttributeSaveDTOv1 src, DataListAttribute dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setAttributeI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n()));
    }
}
