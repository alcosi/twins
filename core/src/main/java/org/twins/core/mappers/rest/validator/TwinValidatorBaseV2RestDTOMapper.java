package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class TwinValidatorBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorBaseDTOv2> {

    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;
    private final TwinValidatorSetRepository twinValidatorSetRepository;


    @Override
    public void map(TwinValidatorEntity src, TwinValidatorBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinValidatorBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst
                .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                        //todo load
                        twinValidatorSetRepository.findById(src.getTwinValidatorSetId()).orElse(null), mapperContext
                ));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
