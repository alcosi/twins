package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FactoryRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryEntity, FactoryDTOv2> {

    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.Factory2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinFactoryEntity src, FactoryDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.Factory2UserMode.HIDE))
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Factory2UserMode.SHORT)))
                    .setCreatedByUserId(src.getCreatedByUserId());
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        factoryRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
