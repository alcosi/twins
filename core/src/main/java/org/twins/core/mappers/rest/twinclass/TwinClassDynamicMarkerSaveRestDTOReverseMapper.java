package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassDynamicMarkerSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerSaveDTOv1, TwinClassDynamicMarkerEntity> {

    @Override
    public void map(TwinClassDynamicMarkerSaveDTOv1 src, TwinClassDynamicMarkerEntity dst, MapperContext mapperContext) throws Exception {
        dst
            .setTwinClassId(src.getTwinClassId())
            .setTwinValidatorSetId(src.getTwinValidatorSetId())
            .setMarkerDataListOptionId(src.getMarkerDataListOptionId());
    }
}
