package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactoryCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactoryCreateRqDTOv1, TwinflowFactoryEntity> {

    private final TwinflowFactorySaveRestDTOReverseMapper twinflowFactorySaveRestDTOReverseMapper;

    @Override
    public void map(TwinflowFactoryCreateRqDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        twinflowFactorySaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
