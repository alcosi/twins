package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFieldEntity, FieldValueText> {
    final FeaturerService featurerService;

    @Override
    public void map(TwinFieldEntity src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = fieldTyper.deserializeValue(src);
        dst.setTwinClassField(fieldValue.getTwinClassField());
        if (fieldValue instanceof FieldValueText text) {
            dst.setValue(text.getValue());
        } else if (fieldValue instanceof FieldValueColorHEX color) {
            dst.setValue(color.getHex());
        } else if (fieldValue instanceof FieldValueDate date) {
            dst.setValue(date.getDate());
        } else if (fieldValue instanceof FieldValueSelect select) {
            dst.setValue(String.join(",", select.getOptions().stream().map(o -> o.getId().toString()).toList()));
        } else if (fieldValue instanceof FieldValueUser userField) {
            List<String> userIdList = new ArrayList<>();
            for (UserEntity userEntity : userField.getUsers()) {
                mapperContext.addRelatedObject(userEntity); // we have to put users to related object
                userIdList.add(userEntity.getId().toString());
            }
            dst.setValue(String.join(",", userIdList));
        } else if (fieldValue instanceof FieldValueLink link) {
            if (link.isForwardLink())
                dst.setValue(String.join(",", link.getTwinLinks().stream().map(l -> l.getDstTwinId().toString()).toList()));
            else
                dst.setValue(String.join(",", link.getTwinLinks().stream().map(l -> l.getSrcTwinId().toString()).toList()));
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, src.easyLog(EasyLoggable.Level.NORMAL) + " unknown value type");
    }
}
