package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.ReplaceOperationRestDTOReverseMapper;
import org.twins.core.service.twinclass.TwinClassService;


@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassUpdateRqDTOv1, TwinClassUpdate> {
    final TwinClassService twinClassService;
    final TwinClassSaveRestDTOReverseMapper twinClassSaveRestDTOReverseMapper;
    final ReplaceOperationRestDTOReverseMapper replaceOperationRestDTOReverseMapper;

    @Override
    public void map(TwinClassUpdateRqDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateTwinClassEntity(twinClassSaveRestDTOReverseMapper.convert(src))
                .setDbTwinClassEntity(twinClassService.findEntitySafe(src.getTwinClassId()))
                .setMarkersReplace(replaceOperationRestDTOReverseMapper.convert(src.getMarkersReplace()))
                .setTagsReplace(replaceOperationRestDTOReverseMapper.convert(src.getTagsReplace()));
    }
}
