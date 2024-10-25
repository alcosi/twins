package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.twin.TwinValidatorSetService;

@Component
@RequiredArgsConstructor
public class TwinValidatorBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorBaseDTOv2> {


    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;
    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.class})
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;
    private final TwinValidatorSetService twinValidatorSetService;


    @Override
    public void map(TwinValidatorEntity src, TwinValidatorBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinValidatorBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TwinValidatorMode.SHORT)) {
            case DETAILED:
                dst
                        .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                                twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.SHORT))))
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
            case SHORT:
                dst
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinValidatorMode.HIDE);
    }

}
