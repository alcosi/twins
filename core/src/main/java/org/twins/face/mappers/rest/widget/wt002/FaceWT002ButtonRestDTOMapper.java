package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonEntity;
import org.twins.face.dto.rest.widget.wt002.FaceWT002ButtonDTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT002ButtonRestDTOMapper extends RestSimpleDTOMapper<FaceWT002ButtonEntity, FaceWT002ButtonDTOv1> {
    private final ResourceService resourceService;

    @MapperModePointerBinding(modes = FaceMode.ModalFace2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(FaceWT002ButtonEntity src, FaceWT002ButtonDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()))
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "));

        if (mapperContext.hasModeButNot(FaceMode.ModalFace2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getModalFace(), mapperContext.forkOnPoint(FaceMode.ModalFace2FaceMode.SHORT));
            dst.setModalFaceId(src.getModalFaceId());
        }
    }
}
