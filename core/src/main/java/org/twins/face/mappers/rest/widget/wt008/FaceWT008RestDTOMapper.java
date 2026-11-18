package org.twins.face.mappers.rest.widget.wt008;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt008.FaceWT008Entity;
import org.twins.face.dto.rest.widget.wt008.FaceWT008DTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT008RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT008Entity>, FaceWT008DTOv1> {

    protected final FaceWidgetRestDTOMapper<FaceWT008Entity> faceWidgetRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT008Entity> src, FaceWT008DTOv1 dst, MapperContext mapperContext) throws Exception {
        FaceWT008Entity config = src.getConfig();
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);

        dst
                .setKey(config.getKey())
                .setLabel(I18nCacheHolder.addId(config.getLabelI18nId()))
                .setTwinCommentSearchId(config.getTwinCommentSearchId())
                .setStyleClasses(StringUtils.splitToSet(config.getStyleClasses(), " "));
    }
}
