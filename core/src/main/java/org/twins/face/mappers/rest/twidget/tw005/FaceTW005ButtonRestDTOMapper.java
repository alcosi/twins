package org.twins.face.mappers.rest.twidget.tw005;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.twidget.tw005.FaceTW005ButtonEntity;
import org.twins.face.dto.rest.twidget.tw005.FaceTW005ButtonDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW005ButtonRestDTOMapper extends RestSimpleDTOMapper<FaceTW005ButtonEntity, FaceTW005ButtonDTOv1> {
    private final ResourceService resourceService;
    @MapperModePointerBinding(modes = FaceTW005Modes.FaceTW005Button2TransitionMode.class)
    private final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;

    @Override
    public void map(FaceTW005ButtonEntity src, FaceTW005ButtonDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTransition().getNameI18NId()))
                .setOrder(src.getOrder())
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setTransitionId(src.getTransitionId())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setShowWhenInactive(src.isShowWhenInactive())
        ;

        if (mapperContext.hasModeButNot(FaceTW005Modes.FaceTW005Button2TransitionMode.HIDE)) {
            twinTransitionRestDTOMapper.postpone(src.getTransition(), mapperContext.forkOnPoint(FaceTW005Modes.FaceTW005Button2TransitionMode.SHORT));
        }
    }
}
