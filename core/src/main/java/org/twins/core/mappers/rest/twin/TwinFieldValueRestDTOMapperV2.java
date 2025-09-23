package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.JsonUtils;
import org.cambium.common.util.MapUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.RelationTwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapperV2 extends RestSimpleDTOMapper<FieldValue, FieldValueText> {
    @MapperModePointerBinding(modes = DataListOptionMode.TwinField2DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinField2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.TwinField2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = RelationTwinMode.TwinByFieldMode.class)
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @Override
    public FieldValueText convert(FieldValue src, MapperContext mapperContext) throws Exception {
        FieldValueText dst = new FieldValueText(src.getTwinClassField());
        if (!src.isFilled()) {
            dst.setValue("");
        } else if (src instanceof FieldValueText text) {
            dst.setValue(text.getValue());
        } else if (src instanceof FieldValueColorHEX color) {
            dst.setValue(color.getHex());
        } else if (src instanceof FieldValueDate date) {
            dst.setValue(date.getDateStr());
        } else if (src instanceof FieldValueInvisible) {
            dst.setValue("");
        } else if (src instanceof FieldValueAttachment fieldValueAttachment) {
            if (fieldValueAttachment.getName() != null && fieldValueAttachment.getBase64Content() != null) {
                dst.setValue(fieldValueAttachment.getBase64Content());
            } else if (fieldValueAttachment.getName() != null) {
                dst.setValue(fieldValueAttachment.getName());
            } else {
                dst.setValue("");
            }
        } else if (src instanceof FieldValueBoolean fieldValueBoolean) {
            dst.setValue(String.valueOf(fieldValueBoolean.getValue()));
        } else if (src instanceof FieldValueTwinClassList fieldValueTwinClassList) {
            dst.setValue(String.valueOf(fieldValueTwinClassList.getTwinClassEntities()));
        } else if (src instanceof FieldValueSelect select) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (DataListOptionEntity dataListOptionEntity : select.getOptions()) {
                stringJoiner.add(dataListOptionEntity.getId().toString());
                if (mapperContext.hasModeButNot(DataListOptionMode.TwinField2DataListOptionMode.HIDE)) {
                    dataListOptionRestDTOMapper.postpone(dataListOptionEntity, mapperContext.forkOnPoint(DataListOptionMode.TwinField2DataListOptionMode.SHORT));
                }
            }
            dst.setValue(stringJoiner.toString());
        } else if (src instanceof FieldValueUser userField) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (UserEntity userEntity : userField.getUsers()) {
                stringJoiner.add(userEntity.getId().toString());
                if (mapperContext.hasModeButNot(UserMode.TwinField2UserMode.HIDE)) {
                    userRestDTOMapper.postpone(userEntity, mapperContext.forkOnPoint(UserMode.TwinField2UserMode.HIDE));
                }
            }
            dst.setValue(stringJoiner.toString());
        } else if (src instanceof FieldValueUserSingle userField) {
            if (mapperContext.hasModeButNot(UserMode.TwinField2UserMode.HIDE)) {
                userRestDTOMapper.postpone(userField.getUser(), mapperContext.forkOnPoint(UserMode.TwinField2UserMode.HIDE));
            }
            dst.setValue(userField.getUser().getId().toString());
        } else if (src instanceof FieldValueStatusSingle statusField) {
            if (mapperContext.hasModeButNot(StatusMode.TwinField2StatusMode.HIDE)) {
                twinStatusRestDTOMapper.postpone(statusField.getStatus(), mapperContext.forkOnPoint(StatusMode.TwinField2StatusMode.HIDE));
            }
            dst.setValue(statusField.getStatus().getId().toString());
        } else if (src instanceof FieldValueLink link) {
            StringJoiner stringJoiner = new StringJoiner(",");
            TwinEntity linkedTwin;
            for (TwinLinkEntity twinLinkEntity : link.getTwinLinks()) {
                if (link.isForwardLink())
                    linkedTwin = twinLinkEntity.getDstTwin();
                else
                    linkedTwin = twinLinkEntity.getSrcTwin();
                stringJoiner.add(linkedTwin.getId().toString());
                if (mapperContext.hasModeButNot(RelationTwinMode.TwinByFieldMode.WHITE)) {
                    twinBaseRestDTOMapper.postpone(linkedTwin, mapperContext.forkOnPoint(RelationTwinMode.TwinByFieldMode.GREEN));
                }
            }
            dst.setValue(stringJoiner.toString());
        } else if (src instanceof FieldValueLinkSingle link) {
            if (mapperContext.hasModeButNot(RelationTwinMode.TwinByFieldMode.WHITE)) {
                twinBaseRestDTOMapper.postpone(link.getDstTwin(), mapperContext.forkOnPoint(RelationTwinMode.TwinByFieldMode.GREEN));
            }
            dst.setValue(link.getDstTwin().getId().toString());
        } else if (src instanceof FieldValueI18n i18nField) {
            if (MapUtils.isNotEmpty(i18nField.getTranslations())) {
                String jsonStr = JsonUtils.translationsMapToJson(i18nField.getTranslations());
                if (jsonStr == null)
                    throw new ServiceException(
                            ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT,
                            src.getTwinClassField().logNormal() + " can't serialize i18n");
                dst.setValue(jsonStr);
            }
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, src.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " unknown value type");

        return dst;
    }


    @Override
    public List<FieldValueText> convertCollection(Collection<FieldValue> srcList, MapperContext mapperContext) throws Exception {
        return super.convertCollection(srcList
                .stream().filter(v -> !(v instanceof FieldValueInvisible) || (v instanceof FieldValueAttachment)).toList(), mapperContext);
    }

    @Override
    public void map(FieldValue src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }
}
