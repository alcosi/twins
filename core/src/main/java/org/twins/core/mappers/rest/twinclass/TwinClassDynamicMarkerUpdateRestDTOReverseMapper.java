package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassDynamicMarkerUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerUpdateDTOv1, TwinClassDynamicMarkerEntity> {

    @Override
    public void map(TwinClassDynamicMarkerUpdateDTOv1 src, TwinClassDynamicMarkerEntity dst, MapperContext mapperContext) throws Exception {
        dst
            .setId(src.getId())
            .setTwinClassId(src.getTwinClassId())
            .setTwinValidatorSetId(src.getTwinValidatorSetId())
            .setMarkerDataListOptionId(src.getMarkerDataListOptionId());
    }
}
