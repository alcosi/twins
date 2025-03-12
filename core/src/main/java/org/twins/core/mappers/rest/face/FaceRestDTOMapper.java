package org.twins.core.mappers.rest.face;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dto.rest.face.FaceViewDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class FaceRestDTOMapper extends RestSimpleDTOMapper<FaceEntity, FaceViewDTOv1> {
    private final FaceBasicRestDTOMapper faceBasicRestDTOMapper;
    @Override
    public void map(FaceEntity src, FaceViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceBasicRestDTOMapper.map(src, dst, mapperContext);
        dst
                .setComponent(src.getFaceComponentId())
                .setConfigId(src.getId());
    }
}
