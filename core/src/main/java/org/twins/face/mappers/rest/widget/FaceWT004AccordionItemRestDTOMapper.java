package org.twins.face.mappers.rest.widget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.widget.FaceWT004AccordionItemEntity;
import org.twins.face.dto.rest.widget.FaceWT004AccordionItemDTOv1;


@Component
@RequiredArgsConstructor
public class FaceWT004AccordionItemRestDTOMapper extends RestSimpleDTOMapper<FaceWT004AccordionItemEntity, FaceWT004AccordionItemDTOv1> {
    private final I18nService i18nService;


    @Override
    public void map(FaceWT004AccordionItemEntity src, FaceWT004AccordionItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setLocale(src.getLocale().toLanguageTag())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId()));
    }


}
