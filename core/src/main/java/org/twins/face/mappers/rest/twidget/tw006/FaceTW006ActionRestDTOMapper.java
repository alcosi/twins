package org.twins.face.mappers.rest.twidget.tw006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006ActionDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW006ActionRestDTOMapper extends RestSimpleDTOMapper<FaceTW006ActionEntity, FaceTW006ActionDTOv1> {

    @Override
    public void map(FaceTW006ActionEntity src, FaceTW006ActionDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setActionId(src.getTwinActionId())
                .setFaceTW006Id(src.getFaceTW006Id())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()));
    }
}
