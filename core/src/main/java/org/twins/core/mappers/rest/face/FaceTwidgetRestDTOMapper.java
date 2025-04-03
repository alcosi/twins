package org.twins.core.mappers.rest.face;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.face.FaceTwidget;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.service.face.FaceService;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceTwidgetRestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<? extends FaceTwidget>, FaceTwidgetDTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceService faceService;
    @MapperModePointerBinding(modes = TwinMode.FaceTwidget2TwinMode.class)
    protected final TwinRestDTOMapper twinRestDTOMapper;

    @Override
    public void map(TwidgetConfig<? extends FaceTwidget> src, FaceTwidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getConfig().getFace(), dst, mapperContext);
        dst.setPointedTwinId(src.getTargetTwinId());
        if (mapperContext.hasModeButNot(TwinMode.FaceTwidget2TwinMode.HIDE)) {
            faceService.loadTwin(src);
            twinRestDTOMapper.postpone(src.getTargetTwin(), mapperContext.forkOnPoint(TwinMode.FaceTwidget2TwinMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwidgetConfig<? extends FaceTwidget>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinMode.FaceTwidget2TwinMode.HIDE)) {
            faceService.loadTwin(srcCollection);
        }
    }
}
