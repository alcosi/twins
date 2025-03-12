package org.twins.core.mappers.rest.face;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dto.rest.face.FaceBasicDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class FaceBasicRestDTOMapper extends RestSimpleDTOMapper<FaceEntity, FaceBasicDTOv1> {
    @Override
    public void map(FaceEntity src, FaceBasicDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setComponent(src.getFaceComponentId())
                .setConfigId(src.getId());
    }
}
