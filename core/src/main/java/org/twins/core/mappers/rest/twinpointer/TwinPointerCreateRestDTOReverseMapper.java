package org.twins.core.mappers.rest.twinpointer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.twin.TwinPointerCreate;
import org.twins.core.dto.rest.twinpointer.TwinPointerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinPointerCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinPointerCreateDTOv1, TwinPointerCreate> {

    @Override
    public void map(TwinPointerCreateDTOv1 src, TwinPointerCreate dst, MapperContext mapperContext) throws Exception {
        dst.setTwinPointer(new TwinPointerEntity()
                .setTwinClassId(src.getTwinClassId())
                .setPointerFeaturerId(src.getPointerFeaturerId())
                .setPointerParams(src.getPointerParams())
                .setName(src.getName())
                .setOptional(BooleanUtils.isTrue(src.getOptional())));
    }
}
