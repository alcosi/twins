package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinClassFieldConditionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionCreateDTOv1, TwinClassFieldConditionEntity> {
    private final TwinClassFieldConditionSaveRestDTOReverseMapper twinClassFieldConditionSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldConditionCreateDTOv1 src, TwinClassFieldConditionEntity dst, MapperContext mapperContext) {
        twinClassFieldConditionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
