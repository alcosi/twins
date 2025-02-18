package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class FactoryConditionSetRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryConditionSetEntity, FactoryConditionSetDTOv2> {
    @MapperModePointerBinding(modes = UserMode.FactoryConditionSet2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @Override
    public void map(TwinFactoryConditionSetEntity src, FactoryConditionSetDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryConditionSetRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.FactoryConditionSet2UserMode.HIDE))
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.FactoryConditionSet2UserMode.HIDE)))
                    .setCreatedByUserId(src.getCreatedByUserId());
    }
}
