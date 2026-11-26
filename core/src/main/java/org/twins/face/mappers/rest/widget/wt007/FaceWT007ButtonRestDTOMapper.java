package org.twins.face.mappers.rest.widget.wt007;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt007.FaceWT007ButtonEntity;
import org.twins.face.dto.rest.widget.wt007.FaceWT007ButtonDTOv1;
import org.twins.face.service.widget.FaceWT007ButtonService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT007ButtonRestDTOMapper extends RestSimpleDTOMapper<FaceWT007ButtonEntity, FaceWT007ButtonDTOv1> {
    private final ResourceService resourceService;
    private final FaceWT007ButtonService faceWT007ButtonService;
    @MapperModePointerBinding(modes = FaceWT007Modes.FaceWT007Button2TransitionMode.class)
    private final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;

    @Override
    public void map(FaceWT007ButtonEntity src, FaceWT007ButtonDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT007ButtonService.loadTransitions(src);
        faceWT007ButtonService.loadIconResources(src);
        dst
                .setId(src.getId())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTransition().getNameI18NId()))
                .setOrder(src.getOrder())
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setTransitionId(src.getTransitionId())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setShowWhenInactive(src.getShowWhenInactive())
        ;

        if (mapperContext.hasModeButNot(FaceWT007Modes.FaceWT007Button2TransitionMode.HIDE)) {
            twinTransitionRestDTOMapper.postpone(src.getTransition(), mapperContext.forkOnPoint(FaceWT007Modes.FaceWT007Button2TransitionMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT007ButtonEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT007ButtonService.loadTransitions(srcCollection);
        faceWT007ButtonService.loadIconResources(srcCollection);
    }
}
