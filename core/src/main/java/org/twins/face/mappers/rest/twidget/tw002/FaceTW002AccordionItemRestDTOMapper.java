package org.twins.face.mappers.rest.twidget.tw002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twiget.tw002.FaceTW002AccordionItemEntity;
import org.twins.face.dto.rest.twidget.tw002.FaceTW002AccordionItemDTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW002AccordionItemRestDTOMapper extends RestSimpleDTOMapper<FaceTW002AccordionItemEntity, FaceTW002AccordionItemDTOv1> {
    private final I18nService i18nService;


    @Override
    public void map(FaceTW002AccordionItemEntity src, FaceTW002AccordionItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setLocale(src.getLocale().toLanguageTag())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId()));
    }


}
