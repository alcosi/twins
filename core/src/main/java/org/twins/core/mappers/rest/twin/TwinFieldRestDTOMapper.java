package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.*;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
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
        switch (mapperProperties.getModeOrUse(Mode.FIELDS_KEY_VALUE_ONLY)) {
            case FIELDS_TYPE_DETAILED:
                twinClassFieldRestDTOMapper.map(src.twinClassField(), dst, mapperProperties);
            case FIELDS_KEY_VALUE_ONLY:
                dst.key(src.twinClassField().key());
                FieldTyper fieldTyper = featurerService.getFeaturer(src.twinClassField().fieldTyperFeaturer(), FieldTyper.class);
                dst.value(twinFieldValueRestDTOMapper.convert(fieldTyper.deserializeValue(src.twinClassField().fieldTyperParams(), src.value())));
        }
    }

    public enum Mode implements MapperMode {
        FIELDS_KEY_VALUE_ONLY, FIELDS_TYPE_DETAILED;

        public static final String _FIELDS_KEY_VALUE_ONLY = "FIELDS_KEY_VALUE_ONLY";
        public static final String _FIELDS_TYPE_DETAILED = "FIELDS_TYPE_DETAILED";

    }
}
