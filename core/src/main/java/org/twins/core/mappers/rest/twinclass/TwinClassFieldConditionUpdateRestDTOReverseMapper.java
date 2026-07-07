package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldConditionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionUpdateDTOv1, TwinClassFieldConditionEntity> {

    private final TwinClassFieldConditionSaveRestDTOReverseMapper twinClassFieldConditionSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldConditionUpdateDTOv1 src, TwinClassFieldConditionEntity dst, MapperContext mapperContext) {
        twinClassFieldConditionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
