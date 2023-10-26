package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapper extends RestSimpleDTOMapper<TwinFieldEntity, TwinFieldDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final FeaturerService featurerService;
    final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    @Override
    public void map(TwinFieldEntity src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        dst
                .id(src.getId())
                .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.SHORT)))
                .value(twinFieldValueRestDTOMapper.convert(fieldTyper.deserializeValue(src, src.getValue())));
    }
}
