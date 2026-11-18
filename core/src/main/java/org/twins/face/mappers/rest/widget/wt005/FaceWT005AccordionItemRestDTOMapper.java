package org.twins.face.mappers.rest.widget.wt005;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt005.FaceWT005AccordionItemEntity;
import org.twins.face.dto.rest.widget.wt005.FaceWT005AccordionItemDTOv1;


@Component
@RequiredArgsConstructor
public class FaceWT005AccordionItemRestDTOMapper extends RestSimpleDTOMapper<FaceWT005AccordionItemEntity, FaceWT005AccordionItemDTOv1> {


    @Override
    public void map(FaceWT005AccordionItemEntity src, FaceWT005AccordionItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setLocale(src.getLocale().toLanguageTag())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()));
    }


}
