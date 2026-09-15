package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapperV2 extends RestSimpleDTOMapper<FieldValueText, FieldValue> {

    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;
    private final FeaturerService featurerService;

    @Override
    public void map(FieldValueText src, FieldValue dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED); // FieldValue is abstract, the value type is decided by parseSingle
    }

    @Override
    public FieldValue convert(FieldValueText src, MapperContext mapperContext) throws Exception {
        // route a single item through the collection path, so afterCollectionConversion materializes it too
        return convertCollection(List.of(src), mapperContext).getFirst();
    }

    @Override
    public List<FieldValue> convertCollection(Collection<FieldValueText> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null)
            return null;
        if (srcCollection.isEmpty())
            return Collections.emptyList();
        beforeCollectionConversion(srcCollection, mapperContext);
        var ret = parse(srcCollection);
        afterCollectionConversion(ret, mapperContext);
        return ret;
    }

    @Override
    public void afterCollectionConversion(Collection<FieldValue> dstCollection, MapperContext mapperContext) throws Exception {
        // batch-level materialization: ONE query per referenced entity type for the whole converted collection
        // (convert() parses only). convertCollection passes its mutable result list, so the in-place swap
        // reaches the caller; convertMap's values() view is not replaceable and is not used for this mapper.
        if (dstCollection instanceof List<FieldValue> values)
            twinService.materializeFieldValues(values);
    }

    public FieldValueText createValueByClassIdAndFieldKey(UUID twinClassId, String fieldKey, String fieldValue) {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKeyIncludeParents(twinClassId, fieldKey);
        if (twinClassFieldEntity == null)
            return null;
        return new FieldValueText(twinClassFieldEntity)
                .setValue(fieldValue);
    }

    public List<FieldValueText> createValuesByClassIdAndFieldKeys(UUID twinClassId, Map<String, String> fieldsMap) {
        if (twinClassId == null || fieldsMap == null || fieldsMap.isEmpty())
            return Collections.emptyList();

        List<TwinClassFieldEntity> fieldsList = twinClassFieldService.findByTwinClassIdAndKeysIncludeParents(twinClassId, fieldsMap.keySet());
        Kit<TwinClassFieldEntity, String> twinClassFieldkit = new Kit<>(fieldsList, TwinClassFieldEntity::getKey);

        List<FieldValueText> result = new ArrayList<>();

        fieldsMap.forEach((key, value) -> {
            TwinClassFieldEntity field = twinClassFieldkit.get(key);
            if (field != null) {
                result.add(new FieldValueText(field).setValue(value));
            }
        });
        return result;
    }

    public List<FieldValueText> createValuesByClassIdAndFieldIds(UUID twinClassId, Map<UUID, String> fieldsMap) {
        if (twinClassId == null || fieldsMap == null || fieldsMap.isEmpty())
            return Collections.emptyList();

        List<TwinClassFieldEntity> fieldsList = twinClassFieldService.findByTwinClassIdAndIdsIncludeParents(twinClassId, fieldsMap.keySet());
        Kit<TwinClassFieldEntity, UUID> twinClassFieldkit = new Kit<>(fieldsList, TwinClassFieldEntity::getId);

        List<FieldValueText> result = new ArrayList<>();

        fieldsMap.forEach((key, value) -> {
            TwinClassFieldEntity field = twinClassFieldkit.get(key);
            if (field != null) {
                result.add(new FieldValueText(field).setValue(value));
            }
        });
        return result;
    }

    public FieldValueText createValueByTwinClassFieldId(UUID twinClassFieldId, String fieldValue) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId);
        if (twinClassFieldEntity == null)
            return null;
        return new FieldValueText(twinClassFieldEntity)
                .setValue(fieldValue);
    }

    public FieldValueText createByTwinIdAndFieldKey(UUID twinId, String fieldKey, String fieldValue) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKeyIncludeParents(twinEntity.getTwinClass(), fieldKey);
        if (twinClassFieldEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN);
        return new FieldValueText(twinClassFieldEntity)
                .setValue(fieldValue);
    }

    public List<FieldValue> mapFields(UUID twinClassId, Map<String, String> fieldsMap) throws Exception {
        return convertCollection(classIdFieldsToTexts(twinClassId, fieldsMap)); // afterCollectionConversion materializes the whole map
    }

    public List<FieldValue> mapFields(Map<UUID, String> fieldsMap) throws Exception { // map key is twinClassFieldId
        return convertCollection(fieldIdFieldsToTexts(fieldsMap)); // afterCollectionConversion materializes the whole map
    }

    // the one and only parse point: string -> FieldValue (reference types come back as FieldValueReference)
    private FieldValue parse(FieldValueText src) throws ServiceException {
        return twinService.parseFieldValue(src.getTwinClassField(), src.getValue());
    }

    private List<FieldValue> parse(Collection<FieldValueText> fields) throws Exception {
        List<FieldValue> ret = new ArrayList<>(fields.size());
        for (FieldValueText src : fields) {
            FieldValue converted = parse(src); // parse only, no materialization — see parseFields
            if (converted != null)
                ret.add(converted);
        }
        return ret;
    }

    /**
     * Parse-only variant for callers that map fields per item of an OUTER batch (e.g. every TwinCreate inside
     * TwinCreateRqRestDTOReverseMapper) and materialize all the collected references once, batch-wide, in their
     * own afterCollectionConversion — going through mapFields/convertCollection here would resolve each inner
     * collection separately and bring the N+1 back.
     */
    public List<FieldValue> parse(UUID twinClassId, Map<String, String> fieldsMap) throws Exception {
        return parse(classIdFieldsToTexts(twinClassId, fieldsMap));
    }

    /**
     * Parse-only variant of {@link #mapFields(Map)} — see {@link #parse(UUID, Map)}.
     */
    public List<FieldValue> parse(Map<UUID, String> fieldsMap) throws Exception {
        return parse(fieldIdFieldsToTexts(fieldsMap));
    }

    private List<FieldValueText> classIdFieldsToTexts(UUID twinClassId, Map<String, String> fieldsMap) throws ServiceException {
        List<FieldValueText> fields = new ArrayList<>();
        if (fieldsMap == null)
            return fields;
        Map<String, String> mapFieldKeys = new HashMap<>();
        Map<UUID, String> mapFieldIds = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
            if (UuidUtils.isUUID(entry.getKey()))
                mapFieldIds.put(UUID.fromString(entry.getKey()), entry.getValue());
            else
                mapFieldKeys.put(entry.getKey(), entry.getValue());
        }
        fields.addAll(createValuesByClassIdAndFieldKeys(twinClassId, mapFieldKeys));
        fields.addAll(createValuesByClassIdAndFieldIds(twinClassId, mapFieldIds));
        return fields;
    }

    private List<FieldValueText> fieldIdFieldsToTexts(Map<UUID, String> fieldsMap) throws ServiceException {
        List<FieldValueText> fields = new ArrayList<>();
        if (fieldsMap != null)
            for (Map.Entry<UUID, String> entry : fieldsMap.entrySet()) {
                if (entry.getValue() == null)
                    continue; //skipping nullable
                CollectionUtils.addIgnoreNull(
                        fields,
                        createValueByTwinClassFieldId(entry.getKey(), entry.getValue()));
            }
        return fields;
    }


}
