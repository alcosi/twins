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
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapperV2 extends RestSimpleDTOMapper<FieldValueText, FieldValue> {

    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;
    private final FeaturerService featurerService;

    @Override
    public void map(FieldValueText src, FieldValue dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(FieldValueText fieldValueText, MapperContext mapperContext) throws Exception {
        return twinService.createFieldValue(fieldValueText.getTwinClassField(), fieldValueText.getValue());
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
            if (value == null)
                return;
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
            if (value == null)
                return;
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
        List<FieldValueText> fields = new ArrayList<>();
        if (fieldsMap == null)
            return convertCollection(fields);
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
        return convertCollection(fields);
    }

    public List<FieldValue> mapFields(Map<UUID, String> fieldsMap) throws Exception { // map key is twinClassFieldId
        List<FieldValueText> fields = new ArrayList<>();
        if (fieldsMap != null)
            for (Map.Entry<UUID, String> entry : fieldsMap.entrySet()) {
                if (entry.getValue() == null)
                    continue; //skipping nullable
                CollectionUtils.addIgnoreNull(
                        fields,
                        createValueByTwinClassFieldId(entry.getKey(), entry.getValue()));
            }
        return convertCollection(fields);
    }
}
