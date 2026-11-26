package org.twins.face.mappers.rest.widget.wt007;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt007.FaceWT007Entity;
import org.twins.face.dto.rest.widget.wt007.FaceWT007DTOv1;
import org.twins.face.service.widget.FaceWT007Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceWT007RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT007Entity>, FaceWT007DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT007Entity> faceWidgetRestDTOMapper;
    protected final FaceWT007Service faceWT007Service;
    protected final FaceWT007ButtonRestDTOMapper faceWT007ButtonRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT007Entity> src, FaceWT007DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case DETAILED -> {
                faceWT007Service.loadButtons(src.getConfig());
                dst
                        .setGlue(src.getConfig().getGlue())
                        .setAlignVertical(src.getConfig().getAlignVertical())
                        .setStyleClasses(StringUtils.splitToSet(src.getConfig().getStyleClasses(), " "))
                        .setButtons(faceWT007ButtonRestDTOMapper.convertCollection(src.getConfig().getButtons(), mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT007Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.getModeOrUse(FaceMode.SHORT) == FaceMode.DETAILED) {
            faceWT007Service.loadButtons(srcCollection.stream().map(PointedFace::getConfig).toList());
        }
    }
}
