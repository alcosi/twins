package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassDynamicMarkerUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerUpdateDTOv1, TwinClassDynamicMarkerEntity> {

    private final TwinClassDynamicMarkerSaveRestDTOReverseMapper twinClassDynamicMarkerSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassDynamicMarkerUpdateDTOv1 src, TwinClassDynamicMarkerEntity dst, MapperContext mapperContext) throws Exception {
        twinClassDynamicMarkerSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
