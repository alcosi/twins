package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {
    final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;
    final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);
        List<TwinField> twinFieldList;
        switch (mapperContext.getModeOrUse(TwinRestDTOMapper.FieldsMode.NO_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinService.loadFieldsValues(src);
                dst.fields(twinFieldValueRestDTOMapperV2.convertList(src.getFieldValuesKit().getCollection(), mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
            case NOT_EMPTY_FIELDS:
                twinService.loadFieldsValues(src);
                List<FieldValue> notEmptyFields = src.getFieldValuesKit().getCollection().stream().filter(FieldValue::isFilled).toList();
                dst.fields(twinFieldValueRestDTOMapperV2.convertList(notEmptyFields, mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
        }
    }

    @Override
    public void beforeListConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.beforeListConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(TwinRestDTOMapper.FieldsMode.ALL_FIELDS) || mapperContext.hasMode(TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS))
            twinService.loadTwinFields(srcCollection); // bulk load (minimizing the number of db queries)
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
