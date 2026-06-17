package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinflow.TransitionUpdate;
import org.twins.core.dto.rest.twinflow.TransitionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TransitionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionUpdateDTOv1, TransitionUpdate> {

    private final TransitionSaveRestDTOReverseMapper transitionSaveRestDTOReverseMapper;


    @Override
    public void map(TransitionUpdateDTOv1 src, TransitionUpdate dst, MapperContext mapperContext) throws Exception {
        transitionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
