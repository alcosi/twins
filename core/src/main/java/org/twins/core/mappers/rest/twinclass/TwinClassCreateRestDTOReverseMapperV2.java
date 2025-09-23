package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassCreate;
import org.twins.core.dto.rest.twinclass.TwinClassCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassCreateRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassCreateDTOv1, TwinClassCreate> {
    private final TwinClassSaveRestDTOReverseMapperV2 twinClassSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassCreateDTOv1 src, TwinClassCreate dst, MapperContext mapperContext) throws Exception {
        twinClassSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setAutoCreatePermission(src.getAutoCreatePermissions())
                .setAutoCreateTwinflow(src.getAutoCreateTwinflow())
                .getTwinClass()
                .setExtendsTwinClassId(src.getExtendsTwinClassId())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setMarkerDataListId(src.getMarkerDataListId())
                .setTagDataListId(src.getTagDataListId());
    }
}
