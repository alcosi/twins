package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinFieldAttributeDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.twin.TwinFieldAttributeService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        TwinFieldCollectionMode.class,
        TwinFieldCollectionMapMode.class,
        TwinFieldCollectionFilterEmptyMode.class,
        TwinFieldCollectionFilterSystemMode.class,
        TwinFieldCollectionFilterRequiredMode.class,
        TwinFieldCollectionFilterFieldScope.class,
        TwinFieldAttributeMode.class})
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {

    private final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;

    private final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    private final TwinService twinService;

    @Lazy
    private final TwinFieldAttributeService twinFieldAttributeService;
    private final TwinFieldAttributeRestDTOMapper twinFieldAttributeRestDTOMapper;

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
                    case ONLY_NOT -> fieldsStream.filter(FieldValue::isNotEmpty);
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
                fieldsStream = switch (mapperContext.getModeOrUse(TwinFieldCollectionFilterFieldScope.ANY)) {
                    case ONLY_DECLARED -> fieldsStream.filter(fieldValue -> fieldValue.getTwinClassField().getTwinClassId().equals(src.getTwinClassId()));
                    case ONLY_INHERITED -> fieldsStream.filter(fieldValue -> !fieldValue.getTwinClassField().getTwinClassId().equals(src.getTwinClassId()));
                    default -> fieldsStream;
                };
                List<FieldValue> fields = fieldsStream.toList();
                mapFieldsToDto(src, dst, mapperContext, fields);
            }
        }
    }

    private void mapFieldsToDto(TwinEntity src, TwinDTOv2 dst, MapperContext mapperContext, Collection<FieldValue> fields) throws Exception {
        TwinFieldCollectionMapMode mapMode = mapperContext.getModeOrUse(TwinFieldCollectionMapMode.KEY);
        var fieldsValues = twinFieldValueRestDTOMapperV2.convertCollection(fields, mapperContext);

        dst
                .setFields(new HashMap<>(fieldsValues.size()))
                .setFieldsMap(new HashMap<>(fieldsValues.size()));

        if (mapperContext.hasMode(TwinFieldAttributeMode.SHOW) && (src.getTwinFieldAttributeKit() == null || src.getTwinFieldAttributeKit().isEmpty())) {
            twinFieldAttributeService.loadAttributes(Collections.singletonList(src));
        }

        for (var fieldValueText : fieldsValues) {
            UUID fieldId = fieldValueText.getTwinClassField().getId();
            String fieldKey = fieldValueText.getTwinClassField().getKey();
            String fieldValue = fieldValueText.getValue();

            TwinFieldDTOv2 fieldDto = new TwinFieldDTOv2()
                    .setKey(fieldKey)
                    .setValue(fieldValue);

            if (mapperContext.hasMode(TwinFieldAttributeMode.SHOW) && src.getTwinFieldAttributeKit() != null && src.getTwinFieldAttributeKit().containsGroupedKey(fieldId)) {
                Map<UUID, TwinFieldAttributeDTOv1> fieldAttributesMap = twinFieldAttributeRestDTOMapper.convertCollection(src.getTwinFieldAttributeKit().getGrouped(fieldId), mapperContext)
                        .stream()
                        .collect(Collectors.toMap(TwinFieldAttributeDTOv1::getId, Function.identity()));

                fieldDto.setFieldAttributes(fieldAttributesMap);
            }
            if (mapMode == TwinFieldCollectionMapMode.KEY) {
                dst.getFields().put(fieldKey, fieldValue);
            } else {
                dst.getFields().put(fieldId.toString(), fieldValue);
            }

            dst.getFieldsMap().put(fieldId, fieldDto);
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        TwinFieldCollectionMode.legacyConverter(mapperContext);
        if (mapperContext.hasMode(TwinFieldCollectionMode.SHOW)) {
            twinService.loadTwinFields(srcCollection); // bulk load (minimizing the number of db queries)

        }
        if (mapperContext.hasMode(TwinFieldAttributeMode.SHOW)) {
            twinFieldAttributeService.loadAttributes(srcCollection);
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
