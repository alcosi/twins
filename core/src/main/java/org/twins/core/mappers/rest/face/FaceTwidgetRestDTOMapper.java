package org.twins.core.mappers.rest.face;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.service.face.FaceService;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceTwidgetRestDTOMapper extends RestSimpleDTOMapper<PointedFace<? extends FacePointedEntity>, FaceTwidgetDTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceService faceService;
    @MapperModePointerBinding(modes = TwinMode.FaceTwidget2TwinMode.class)
    protected final TwinRestDTOMapperV2 twinRestDTOMapper;

    @Override
    public void map(PointedFace<? extends FacePointedEntity> src, FaceTwidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getConfig().getFace(), dst, mapperContext);
        dst.setPointedTwinId(src.getTargetTwinId());
        if (mapperContext.hasModeButNot(TwinMode.FaceTwidget2TwinMode.HIDE)) {
            faceService.loadTwin(src);
            twinRestDTOMapper.postpone(src.getTargetTwin(), mapperContext.forkOnPoint(TwinMode.FaceTwidget2TwinMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<? extends FacePointedEntity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinMode.FaceTwidget2TwinMode.HIDE)) {
            faceService.loadTwin(srcCollection);
        }
    }
}
