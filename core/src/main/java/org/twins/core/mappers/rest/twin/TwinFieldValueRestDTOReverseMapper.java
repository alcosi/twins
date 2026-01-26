package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twinclass.TwinClassFieldService;


@Component
@RequiredArgsConstructor
@Deprecated
public class TwinFieldValueRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldValueDTO, FieldValue> {

    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void map(TwinFieldValueDTO src, FieldValue dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(TwinFieldValueDTO fieldValueDTO, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = null;
        if (fieldValueDTO.twinClassId == null || StringUtils.isNoneBlank(fieldValueDTO.fieldKey))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN);
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKeyIncludeParents(fieldValueDTO.twinClassId, fieldValueDTO.fieldKey);
        if (fieldValueDTO instanceof TwinFieldValueTextDTOv1 text)
            fieldValue =  new FieldValueText(twinClassFieldEntity)
                    .setValue(text.text());
        if (fieldValueDTO instanceof TwinFieldValueColorHexDTOv1 color)
            fieldValue =  new FieldValueColorHEX(twinClassFieldEntity)
                    .setHex(color.hex());
        if (fieldValueDTO instanceof TwinFieldValueDateDTOv1 date)
            fieldValue = new FieldValueDate(twinClassFieldEntity)
                    .setDateStr(date.date());
        if (fieldValueDTO instanceof TwinFieldValueListDTOv1 select) {
            fieldValue = new FieldValueSelect(twinClassFieldEntity);
            for (DataListOptionDTOv1 dataListOptionDTO : select.selectedOptions()) {
                ((FieldValueSelect)fieldValue).add(new DataListOptionEntity()
                        .setId(dataListOptionDTO.getId())
                        .setOption(dataListOptionDTO.getName()));
            }
        }

        return fieldValue;
    }
}
