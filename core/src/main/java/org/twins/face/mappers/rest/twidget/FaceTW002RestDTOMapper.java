package org.twins.face.mappers.rest.twidget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twiget.FaceTW002Entity;
import org.twins.face.dto.rest.twidget.FaceTW002DTOv1;
import org.twins.face.mappers.rest.widget.FaceWT001Modes;
import org.twins.face.service.twidget.FaceTW002Service;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceWT001Modes.FaceWT001AccordionItemCollectionMode.class})
public class FaceTW002RestDTOMapper extends RestSimpleDTOMapper<FaceTW002Entity, FaceTW002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final FaceTW002Service faceTW002Service;
    private final I18nService i18nService;
    private final FaceTW002AccordionItemRestDTOMapper faceTW002AccordionItemRestDTOMapper;


    @Override
    public void map(FaceTW002Entity src, FaceTW002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> dst
                    .setKey(src.getKey())
                    .setLabel(i18nService.translateToLocale(src.getLabelI18nId() != null ?
                            src.getLabelI18nId() : src.getI18nTwinClassField().getNameI18nId()))
                    .setI18nTwinClassFieldId(src.getI18nTwinClassFieldId())
                    .setI18nTwinClassFieldKey(src.getI18nTwinClassField().getKey());
        }
        if (mapperContext.hasModeButNot(FaceWT001Modes.FaceWT001AccordionItemCollectionMode.HIDE)) {
            faceTW002Service.loadAccordionItems(src);
            dst.setAccordionItems(faceTW002AccordionItemRestDTOMapper.convertCollection(src.getAccordionItems(), mapperContext));
        }
    }
}
