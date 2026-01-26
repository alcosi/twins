package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowFactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowFactoryMode.class)
public class TwinflowFactoryRestDTOMapperV1 extends RestSimpleDTOMapper<TwinflowFactoryEntity, TwinflowFactoryDTOv1> {

    @MapperModePointerBinding(modes = TwinflowMode.TwinflowFactory2TwinflowMode.class)
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.TwinflowFactory2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapperV2;

    @Override
    public void map(TwinflowFactoryEntity src, TwinflowFactoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowFactoryMode.SHORT)) {
            case SHORT -> dst.setId(src.getId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinflowId(src.getTwinflowId())
                    .setTwinFactoryLauncherId(src.getTwinFactoryLauncher())
                    .setFactoryId(src.getTwinFactoryId());
        }

        if (mapperContext.hasModeButNot(TwinflowMode.TwinflowFactory2TwinflowMode.HIDE)) {
            dst.setTwinflowId(src.getTwinflowId());
            twinflowBaseV1RestDTOMapper.postpone(src.getTwinflow(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinflowMode.TwinflowFactory2TwinflowMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FactoryMode.TwinflowFactory2FactoryMode.HIDE)) {
            dst.setFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapperV2.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.TwinflowFactory2FactoryMode.SHORT)));
        }
    }
}
