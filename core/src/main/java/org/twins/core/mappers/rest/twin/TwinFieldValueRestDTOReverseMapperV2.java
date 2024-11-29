package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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
        FieldTyper fieldTyper = featurerService.getFeaturer(fieldValueText.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = null;
        if (fieldTyper.getValueType() == FieldValueText.class)
            fieldValue = fieldValueText;
        if (fieldTyper.getValueType() == FieldValueColorHEX.class)
            fieldValue = new FieldValueColorHEX(fieldValueText.getTwinClassField());
        if (fieldTyper.getValueType() == FieldValueDate.class)
            fieldValue = new FieldValueDate(fieldValueText.getTwinClassField());
        if (fieldTyper.getValueType() == FieldValueSelect.class)
            fieldValue = new FieldValueSelect(fieldValueText.getTwinClassField());
        if (fieldTyper.getValueType() == FieldValueUser.class)
            fieldValue = new FieldValueUser(fieldValueText.getTwinClassField());
        if (fieldTyper.getValueType() == FieldValueLink.class)
            fieldValue = new FieldValueLink(fieldValueText.getTwinClassField());
        if (fieldTyper.getValueType() == FieldValueInvisible.class)
            fieldValue = new FieldValueInvisible(fieldValueText.getTwinClassField());
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unknown fieldTyper[" + fieldTyper.getValueType() + "]");

        if (fieldValueText.getValue() == null) // nullify
            fieldValue.nullify();
        else
            setValue(fieldValue, fieldValueText.getValue());
        return fieldValue;
    }

    private void setValue(FieldValue fieldValue, String value) throws ServiceException {
        if (fieldValue instanceof FieldValueText fieldValueText)
            fieldValueText.setValue(value);
        if (fieldValue instanceof FieldValueColorHEX fieldValueColorHEX)
            fieldValueColorHEX.setHex(value);
        if (fieldValue instanceof FieldValueDate fieldValueDate)
            fieldValueDate.setDate(value);
        if (fieldValue instanceof FieldValueSelect fieldValueSelect){
            for (String dataListOption : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dataListOption)) continue;
                DataListOptionEntity dataListOptionEntity = new DataListOptionEntity();
                if (UuidUtils.isUUID(dataListOption)) dataListOptionEntity.setId(UUID.fromString(dataListOption));
                else dataListOptionEntity.setOption(dataListOption);
                fieldValueSelect.add(dataListOptionEntity);
            }
        }
        if (fieldValue instanceof FieldValueUser fieldValueUser){
            for (String userId : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(userId))
                    continue;
                UUID userUUID;
                try {
                    userUUID = UUID.fromString(userId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueUser.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect user UUID[" + userId + "]");
                }
                fieldValueUser.add(new UserEntity()
                        .setId(userUUID));
            }
        }
        if (fieldValue instanceof FieldValueLink fieldValueLink){
            for (String dstTwinId : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dstTwinId))
                    continue;
                UUID dstTwinUUID;
                try {
                    dstTwinUUID = UUID.fromString(dstTwinId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueLink.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect link UUID[" + dstTwinId + "]");
                }
                ((FieldValueLink) fieldValue).add(new TwinLinkEntity()
                        .setDstTwinId(dstTwinUUID));
            }
        }
    }

    public FieldValueText createValueByClassIdAndFieldKey(UUID twinClassId, String fieldKey, String fieldValue) {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(twinClassId, fieldKey);
        if (twinClassFieldEntity == null)
            return null;
        return new FieldValueText(twinClassFieldEntity)
                .setValue(fieldValue);
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
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(twinEntity.getTwinClassId(), fieldKey);
        if (twinClassFieldEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN);
        return new FieldValueText(twinClassFieldEntity)
                .setValue(fieldValue);
    }

    public List<FieldValue> mapFields(UUID twinClassId, Map<String, String> fieldsMap) throws Exception {
        List<FieldValueText> fields = new ArrayList<>();
        if (fieldsMap != null)
            for (Map.Entry<String, String> entry : fieldsMap.entrySet())
                CollectionUtils.addIgnoreNull(
                        fields,
                        createValueByClassIdAndFieldKey(twinClassId, entry.getKey(), entry.getValue()));
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
