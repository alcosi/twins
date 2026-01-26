package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassCreate;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassCreateRqDTOv1, TwinClassCreate> {

    private final TwinClassSaveRestDTOReverseMapper twinClassSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassCreateRqDTOv1 src, TwinClassCreate dst, MapperContext mapperContext) throws Exception {
        twinClassSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setAutoCreatePermission(src.getAutoCreatePermissions())

                .getTwinClass()
                .setExtendsTwinClassId(src.getExtendsTwinClassId())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setMarkerDataListId(src.getMarkerDataListId())
                .setTagDataListId(src.getTagDataListId())
        ;
    }
}
