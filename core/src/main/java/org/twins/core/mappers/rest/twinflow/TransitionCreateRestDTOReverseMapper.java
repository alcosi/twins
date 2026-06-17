package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinflow.TransitionCreate;
import org.twins.core.dto.rest.twinflow.TransitionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TransitionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionCreateDTOv1, TransitionCreate> {

    private final TransitionSaveRestDTOReverseMapper transitionSaveRestDTOReverseMapper;


    @Override
    public void map(TransitionCreateDTOv1 src, TransitionCreate dst, MapperContext mapperContext) throws Exception {
        transitionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
