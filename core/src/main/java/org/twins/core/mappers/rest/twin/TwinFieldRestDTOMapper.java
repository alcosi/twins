package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapper extends RestSimpleDTOMapper<TwinFieldEntity, TwinFieldDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final FeaturerService featurerService;
    final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    @Override
    public void map(TwinFieldEntity src, TwinFieldDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.twinClassField().fieldTyperFeaturer(), FieldTyper.class);
        dst
                .id(src.id())
                .twinClassField(twinClassFieldRestDTOMapper.convert(src.twinClassField(), mapperProperties.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.ID_KEY_ONLY)))
                .value(twinFieldValueRestDTOMapper.convert(fieldTyper.deserializeValue(src, src.value())));
    }
}
