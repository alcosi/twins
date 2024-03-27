package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
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
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapperV2 extends RestSimpleDTOMapper<FieldValueText, FieldValue> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final FeaturerService featurerService;

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
            fieldValue = new FieldValueColorHEX(fieldValueText.getTwinClassField())
                    .setHex(fieldValueText.getValue());
        if (fieldTyper.getValueType() == FieldValueDate.class)
            fieldValue = new FieldValueDate(fieldValueText.getTwinClassField())
                    .setDate(fieldValueText.getValue());
        if (fieldTyper.getValueType() == FieldValueSelect.class) {
            fieldValue = new FieldValueSelect(fieldValueText.getTwinClassField());
            for (String dataListOptionId : fieldValueText.getValue().split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dataListOptionId))
                    continue;
                UUID dataListOptionUUID;
                try {
                    dataListOptionUUID = UUID.fromString(dataListOptionId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueText.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect datalist UUID[" + dataListOptionId + "]");
                }
                ((FieldValueSelect) fieldValue).add(new DataListOptionEntity()
                        .setId(dataListOptionUUID));
            }
        }
        if (fieldTyper.getValueType() == FieldValueUser.class) {
            fieldValue = new FieldValueUser(fieldValueText.getTwinClassField());
            for (String userId : fieldValueText.getValue().split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(userId))
                    continue;
                UUID userUUID;
                try {
                    userUUID = UUID.fromString(userId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueText.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect user UUID[" + userId + "]");
                }
                ((FieldValueUser) fieldValue).add(new UserEntity()
                        .setId(userUUID));
            }
        }
        if (fieldTyper.getValueType() == FieldValueLink.class) {
            fieldValue = new FieldValueLink(fieldValueText.getTwinClassField());
            for (String dstTwinId : fieldValueText.getValue().split(FieldTyperList.LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dstTwinId))
                    continue;
                UUID dstTwinUUID;
                try {
                    dstTwinUUID = UUID.fromString(dstTwinId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueText.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect link UUID[" + dstTwinId + "]");
                }
                ((FieldValueLink) fieldValue).add(new TwinLinkEntity()
                        .setDstTwinId(dstTwinUUID));
            }
        }
        if (fieldTyper.getValueType() == FieldValueInvisible.class)
            fieldValue = new FieldValueInvisible(fieldValueText.getTwinClassField());
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unknown fieldTyper[" + fieldTyper.getValueType() + "]");
        return fieldValue;
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
        return convertList(fields);
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
        return convertList(fields);
    }
}
