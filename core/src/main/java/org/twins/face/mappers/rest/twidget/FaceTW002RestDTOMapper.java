package org.twins.face.mappers.rest.twidget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twiget.FaceTW002Entity;
import org.twins.face.dto.rest.twidget.FaceTW002DTOv1;
import org.twins.face.mappers.rest.widget.FaceWT001Modes;
import org.twins.face.service.twidget.FaceTW002Service;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceWT001Modes.FaceWT001AccordionItemCollectionMode.class})
public class FaceTW002RestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<FaceTW002Entity>, FaceTW002DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW002Service faceTW002Service;
    private final I18nService i18nService;
    private final FaceTW002AccordionItemRestDTOMapper faceTW002AccordionItemRestDTOMapper;
    @MapperModePointerBinding(modes = FaceTW002Modes.FaceTW0022TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;


    @Override
    public void map(TwidgetConfig<FaceTW002Entity> src, FaceTW002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> dst
                    .setKey(src.getConfig().getKey())
                    .setLabel(i18nService.translateToLocale(src.getConfig().getLabelI18nId() != null ?
                            src.getConfig().getLabelI18nId() : src.getConfig().getI18nTwinClassField().getNameI18nId()))
                    .setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
        }
        if (mapperContext.hasModeButNot(FaceWT001Modes.FaceWT001AccordionItemCollectionMode.HIDE)) {
            faceTW002Service.loadAccordionItems(src.getConfig());
            dst.setAccordionItems(faceTW002AccordionItemRestDTOMapper.convertCollection(src.getConfig().getAccordionItems(), mapperContext));
        }
        if (mapperContext.hasModeButNot(FaceTW002Modes.FaceTW0022TwinClassFieldMode.HIDE)) {
            dst.setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getI18nTwinClassField(), mapperContext.forkOnPoint(FaceTW002Modes.FaceTW0022TwinClassFieldMode.SHORT));
        }
    }
}
