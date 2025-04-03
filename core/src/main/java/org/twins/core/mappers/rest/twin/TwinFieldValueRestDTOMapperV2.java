package org.twins.core.mappers.rest.twin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapperV2 extends RestSimpleDTOMapper<FieldValue, FieldValueText> {
    @MapperModePointerBinding(modes = DataListOptionMode.TwinField2DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinField2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.TwinField2TwinMode.class)
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
            dst.setValue(date.getDate());
        } else if (src instanceof FieldValueInvisible) {
            dst.setValue("");
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
        } else if (src instanceof FieldValueLink link) {
            StringJoiner stringJoiner = new StringJoiner(",");
            TwinEntity linkedTwin;
            for (TwinLinkEntity twinLinkEntity : link.getTwinLinks()) {
                if (link.isForwardLink())
                    linkedTwin = twinLinkEntity.getDstTwin();
                else
                    linkedTwin = twinLinkEntity.getSrcTwin();
                stringJoiner.add(linkedTwin.getId().toString());
                if (mapperContext.hasModeButNot(TwinMode.TwinField2TwinMode.HIDE)) {
                    twinBaseRestDTOMapper.postpone(linkedTwin, mapperContext.forkOnPoint(UserMode.TwinField2UserMode.HIDE));
                }
            }
            dst.setValue(stringJoiner.toString());
        } else if (src instanceof FieldValueI18n i18nField) {
            try {
                if (MapUtils.isNotEmpty(i18nField.getTranslations())) {
                    Map<String, String> translationsMap = i18nField.getTranslations()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey().toString(),
                                    Map.Entry::getValue
                            ));

                    String jsonStr = new ObjectMapper().writeValueAsString(translationsMap);
                    dst.setValue(jsonStr);
                }
            } catch (JsonProcessingException e) {
                throw new ServiceException(
                        ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT,
                        src.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " can't serialize i18n"
                );
            }
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, src.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " unknown value type");

        return dst;
    }


    @Override
    public List<FieldValueText> convertCollection(Collection<FieldValue> srcList, MapperContext mapperContext) throws Exception {
        return super.convertCollection(srcList
                .stream().filter(v -> !(v instanceof FieldValueInvisible)).toList(), mapperContext);
    }

    @Override
    public void map(FieldValue src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }
}
