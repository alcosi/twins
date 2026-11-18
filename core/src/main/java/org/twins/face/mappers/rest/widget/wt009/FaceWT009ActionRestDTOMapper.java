package org.twins.face.mappers.rest.widget.wt009;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt009.FaceWT009ActionEntity;
import org.twins.face.dto.rest.widget.wt009.FaceWT009ActionDTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT009ActionRestDTOMapper extends RestSimpleDTOMapper<FaceWT009ActionEntity, FaceWT009ActionDTOv1> {

    @Override
    public void map(FaceWT009ActionEntity src, FaceWT009ActionDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setActionId(src.getTwinActionId())
                .setFaceWT009Id(src.getFaceWT009Id())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()));
    }
}
