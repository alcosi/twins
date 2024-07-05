package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinFieldCollectionMode;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinFieldCollectionMode.class})
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {

    private final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;

    private final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    private final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TwinFieldCollectionMode.NO_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinService.loadFieldsValues(src);
                dst.fields(twinFieldValueRestDTOMapperV2.convertCollection(src.getFieldValuesKit().getCollection(), mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
            case NOT_EMPTY_FIELDS:
                twinService.loadFieldsValues(src);
                List<FieldValue> notEmptyFields = src.getFieldValuesKit().getCollection().stream().filter(FieldValue::isFilled).toList();
                dst.fields(twinFieldValueRestDTOMapperV2.convertCollection(notEmptyFields, mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(TwinFieldCollectionMode.ALL_FIELDS) || mapperContext.hasMode(TwinFieldCollectionMode.NOT_EMPTY_FIELDS))
            twinService.loadTwinFields(srcCollection); // bulk load (minimizing the number of db queries)
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
