package org.twins.core.mappers.rest.face;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FaceMode.class)
public class FaceRestDTOMapper extends RestSimpleDTOMapper<FaceEntity, FaceDTOv1> {
    @Override
    public void map(FaceEntity src, FaceDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case DETAILED -> dst
                    .setComponent(src.getFaceComponentId())
                    .setId(src.getId())
                    .setName(src.getName())
                    .setDescription(src.getDescription())
                    .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                    .setCreatedByUserId(src.getCreatedByUserId());
            case SHORT -> dst
                    .setComponent(src.getFaceComponentId())
                    .setId(src.getId());
        }
        //todo Face2UserPointer
    }
}
