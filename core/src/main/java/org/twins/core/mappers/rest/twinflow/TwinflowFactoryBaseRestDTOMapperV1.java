package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowFactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowFactoryMode.class)
public class TwinflowFactoryBaseRestDTOMapperV1 extends RestSimpleDTOMapper<TwinflowFactoryEntity, TwinflowFactoryBaseDTOv1> {

    @MapperModePointerBinding(modes = TwinflowMode.TwinflowFactory2TwinflowMode.class)
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.TwinflowFactory2FactoryMode.class)
    private final FactoryRestDTOMapperV2 factoryRestDTOMapperV2;

    @Override
    public void map(TwinflowFactoryEntity src, TwinflowFactoryBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowFactoryMode.SHORT)) {
            case SHORT -> dst.setId(src.getId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinflowId(src.getTwinflowId())
                    .setTwinFactoryLauncherId(src.getTwinFactorylauncher())
                    .setTwinFactoryId(src.getTwinFactoryId());
        }

        if (mapperContext.hasModeButNot(TwinflowMode.TwinflowFactory2TwinflowMode.HIDE)) {
            dst
                    .setTwinflowId(src.getTwinflowId());

            twinflowBaseV1RestDTOMapper.postpone(src.getTwinflow(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinflowMode.TwinflowFactory2TwinflowMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FactoryMode.TwinflowFactory2FactoryMode.HIDE)) {
            dst
                    .setTwinFactoryId(src.getTwinFactoryId());

            factoryRestDTOMapperV2.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.TwinflowFactory2FactoryMode.SHORT)));
        }
    }
}
