package org.twins.face.mappers.rest.widget.wt003;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dto.rest.widget.wt003.FaceWT003DTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT003RestDTOMapper extends RestSimpleDTOMapper<FaceWT003Entity, FaceWT003DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(FaceWT003Entity src, FaceWT003DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        dst
                .setLevel(src.getLevel())
                .setMessageI18nId(src.getMessageI18nId())
                .setIconResourceId(src.getIconResourceId())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "));
    }
}
