package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinFieldDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        TwinFieldCollectionMode.class,
        TwinFieldCollectionMapMode.class,
        TwinFieldCollectionFilterEmptyMode.class,
        TwinFieldCollectionFilterSystemMode.class,
        TwinFieldCollectionFilterRequiredMode.class})
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {

    private final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;

    private final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    private final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);
        TwinFieldCollectionMode.legacyConverter(mapperContext);
        switch (mapperContext.getModeOrUse(TwinFieldCollectionMode.HIDE)) {
            case HIDE -> {
            }
            case SHOW -> {
                twinService.loadFieldsValues(src);
                Stream<FieldValue> fieldsStream = src.getFieldValuesKit().getCollection().stream()
                        .filter(not(FieldValue::isBaseField));
                fieldsStream = switch (mapperContext.getModeOrUse(TwinFieldCollectionFilterEmptyMode.ANY)) {
                    case ONLY -> fieldsStream.filter(FieldValue::isEmpty); //perhaps we need !isFilled
                    case ONLY_NOT -> fieldsStream.filter(FieldValue::isFilled);
                    default -> fieldsStream;
                };
                fieldsStream = switch (mapperContext.getModeOrUse(TwinFieldCollectionFilterRequiredMode.ANY)) {
                    case ONLY -> fieldsStream.filter(fieldValue -> fieldValue.getTwinClassField().getRequired());
                    case ONLY_NOT -> fieldsStream.filter(fieldValue -> !fieldValue.getTwinClassField().getRequired());
                    default -> fieldsStream;
                };
                fieldsStream = switch (mapperContext.getModeOrUse(TwinFieldCollectionFilterSystemMode.ANY)) {
                    case ONLY -> fieldsStream.filter(fieldValue -> fieldValue.getTwinClassField().getSystem());
                    case ONLY_NOT -> fieldsStream.filter(fieldValue -> !fieldValue.getTwinClassField().getSystem());
                    default -> fieldsStream;
                };
                List<FieldValue> fields = fieldsStream.toList();
                mapFieldsToDto(dst, mapperContext, fields);
            }
        }
    }

    private void mapFieldsToDto(TwinDTOv2 dst, MapperContext mapperContext, Collection<FieldValue> fields) throws Exception {
        TwinFieldCollectionMapMode mapMode = mapperContext.getModeOrUse(TwinFieldCollectionMapMode.KEY);
        var fieldsValues = twinFieldValueRestDTOMapperV2.convertCollection(fields, mapperContext);
        dst
                .fields(new HashMap<>(fieldsValues.size()))
                .fieldsMap(new HashMap<>(fieldsValues.size()));
        for (var fieldValueText : fieldsValues) {
            if (mapMode == TwinFieldCollectionMapMode.KEY) {
                dst.fields().put(fieldValueText.getTwinClassField().getKey(), fieldValueText.getValue());
            } else {
                dst.fields().put(fieldValueText.getTwinClassField().getId().toString(), fieldValueText.getValue());
            }
            dst.fieldsMap().put(fieldValueText.getTwinClassField().getId(), new TwinFieldDTOv2()
                    .setKey(fieldValueText.getTwinClassField().getKey())
                    .setValue(fieldValueText.getValue()));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        TwinFieldCollectionMode.legacyConverter(mapperContext);
        if (mapperContext.hasMode(TwinFieldCollectionMode.SHOW))
            twinService.loadTwinFields(srcCollection); // bulk load (minimizing the number of db queries)
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
