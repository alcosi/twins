package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFieldEntity, FieldValueText> {
    final FeaturerService featurerService;

    @Override
    public void map(TwinFieldEntity src, FieldValueText dst, MapperProperties mapperProperties) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.twinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = fieldTyper.deserializeValue(src, src.value());
        dst.setTwinClassField(fieldValue.getTwinClassField());
        if (fieldValue instanceof FieldValueText text)
            dst.setValue(text.getValue());
        if (fieldValue instanceof FieldValueColorHEX color)
            dst.setValue(color.hex());
        if (fieldValue instanceof FieldValueDate date)
            dst.setValue(date.date());
        if (fieldValue instanceof FieldValueSelect select)
            dst.setValue(String.join(",", select.options().stream().map(o -> o.getId().toString()).toList()));
    }
}
