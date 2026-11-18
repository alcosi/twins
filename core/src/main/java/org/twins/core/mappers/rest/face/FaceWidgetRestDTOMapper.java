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
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceWidgetRestDTOMapper<E extends FacePointedEntity> extends RestSimpleDTOMapper<PointedFace<E>, FaceWidgetDTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceService faceService;
    @MapperModePointerBinding(modes = TwinMode.FaceWidget2TwinMode.class)
    protected final TwinRestDTOMapperV2 twinRestDTOMapper;

    @Override
    public void map(PointedFace<E> src, FaceWidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getConfig().getFace(), dst, mapperContext);
        dst.setPointedTwinId(src.getTargetTwinId());
        if (mapperContext.hasModeButNot(TwinMode.FaceWidget2TwinMode.HIDE)) {
            faceService.loadTwin(src);
            twinRestDTOMapper.postpone(src.getTargetTwin(), mapperContext.forkOnPoint(TwinMode.FaceWidget2TwinMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<E>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinMode.FaceWidget2TwinMode.HIDE)) {
            faceService.loadTwin(srcCollection);
        }
    }
}
