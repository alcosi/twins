package org.twins.face.mappers.rest.widget.wt009;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt009.FaceWT009Entity;
import org.twins.face.dto.rest.widget.wt009.FaceWT009DTOv1;
import org.twins.face.service.widget.FaceWT009Service;

@Component
@RequiredArgsConstructor
public class FaceWT009RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT009Entity>, FaceWT009DTOv1> {

    private final FaceWidgetRestDTOMapper<FaceWT009Entity> faceWidgetRestDTOMapper;
    private final FaceWT009ActionRestDTOMapper faceWT009ActionRestDTOMapper;
    private final FaceWT009Service faceWT009Service;

    @Override
    public void map(PointedFace<FaceWT009Entity> src, FaceWT009DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);

        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case DETAILED -> {
                faceWT009Service.loadActions(src.getConfig());

                dst
                        .setActions(faceWT009ActionRestDTOMapper.convertCollection(src.getConfig().getActions()));
            }
        }
    }
}
