package org.twins.face.mappers.rest.twidget.tw002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.twidget.tw002.FaceTW002AccordionItemEntity;
import org.twins.face.dto.rest.twidget.tw002.FaceTW002AccordionItemDTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW002AccordionItemRestDTOMapper extends RestSimpleDTOMapper<FaceTW002AccordionItemEntity, FaceTW002AccordionItemDTOv1> {


    @Override
    public void map(FaceTW002AccordionItemEntity src, FaceTW002AccordionItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setLocale(src.getLocale().toLanguageTag())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()));
    }


}
