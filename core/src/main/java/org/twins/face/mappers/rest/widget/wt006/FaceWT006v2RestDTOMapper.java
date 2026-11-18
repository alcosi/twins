package org.twins.face.mappers.rest.widget.wt006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt006.FaceWT006Entity;
import org.twins.face.domain.widget.wt006.FaceWT006TwinClassField;
import org.twins.face.dto.rest.widget.wt006.FaceWT006DTOv2;
import org.twins.face.service.widget.FaceWT006Service;

import java.util.List;


@Component
@RequiredArgsConstructor
public class FaceWT006v2RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT006Entity>, FaceWT006DTOv2> {
    protected final FaceWidgetRestDTOMapper<FaceWT006Entity> faceWidgetRestDTOMapper;
    private final FaceWT006Service faceWT006Service;
    private final FaceWT006v2FieldRestDTOMapper faceWT006v2FieldRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT006Entity> src, FaceWT006DTOv2 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        List<FaceWT006TwinClassField> fields = faceWT006Service.loadFields(src);

        dst
                .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelI18nId()))
                .setStyleClasses(src.getConfig().getStyleClasses())
                .setFields(faceWT006v2FieldRestDTOMapper.convertCollection(fields));
    }
}
