package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapperV2 extends RestSimpleDTOMapper<FieldValue, FieldValueText> {
    final TwinService twinService;

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
            dst.setValue(String.join(",", select.getOptions().stream().map(o -> o.getId().toString()).toList()));
        } else if (src instanceof FieldValueUser userField) {
            List<String> userIdList = new ArrayList<>();
            for (UserEntity userEntity : userField.getUsers()) {
                mapperContext.addRelatedObject(userEntity); // we have to put users to related object
                userIdList.add(userEntity.getId().toString());
            }
            dst.setValue(String.join(",", userIdList));
        } else if (src instanceof FieldValueLink link) {
            if (link.isForwardLink())
                dst.setValue(String.join(",", link.getTwinLinks().stream().map(l -> l.getDstTwinId().toString()).toList()));
            else
                dst.setValue(String.join(",", link.getTwinLinks().stream().map(l -> l.getSrcTwinId().toString()).toList()));
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, src.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " unknown value type");
        return dst;
    }

    @Override
    public List<FieldValueText> convertList(Collection<FieldValue> srcList, MapperContext mapperContext) throws Exception {
        return super.convertList(srcList
                .stream().filter(v -> !(v instanceof FieldValueInvisible)).toList(), mapperContext);
    }

    @Override
    public void map(FieldValue src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }
}
