package org.twins.core.mappers.rest.twinpointer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.twin.TwinPointerUpdate;
import org.twins.core.dto.rest.twinpointer.TwinPointerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinPointerUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinPointerUpdateDTOv1, TwinPointerUpdate> {

    @Override
    public void map(TwinPointerUpdateDTOv1 src, TwinPointerUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setTwinPointer(new TwinPointerEntity()
                .setId(src.getId())
                .setTwinClassId(src.getTwinClassId())
                .setPointerFeaturerId(src.getPointerFeaturerId())
                .setPointerParams(src.getPointerParams())
                .setName(src.getName())
                .setOptional(BooleanUtils.isTrue(src.getOptional())));
    }
}
