package org.twins.face.mappers.rest.twidget.tw002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.twidget.tw002.FaceTW002Entity;
import org.twins.face.dto.rest.twidget.tw002.FaceTW002DTOv1;
import org.twins.face.service.twidget.FaceTW002Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceTW002RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW002Entity>, FaceTW002DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW002Service faceTW002Service;
    private final FaceTW002AccordionItemRestDTOMapper faceTW002AccordionItemRestDTOMapper;
    @MapperModePointerBinding(modes = FaceTW002Modes.FaceTW0022TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;


    @Override
    public void map(PointedFace<FaceTW002Entity> src, FaceTW002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> dst
                    .setKey(src.getConfig().getKey())
                    .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelI18nId() != null ?
                            src.getConfig().getLabelI18nId() : src.getConfig().getI18nTwinClassField().getNameI18nId()))
                    .setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
        }
        faceTW002Service.loadAccordionItems(src.getConfig());
        dst.setAccordionItems(faceTW002AccordionItemRestDTOMapper.convertCollection(src.getConfig().getAccordionItems(), mapperContext));
        if (mapperContext.hasModeButNot(FaceTW002Modes.FaceTW0022TwinClassFieldMode.HIDE)) {
            dst.setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getI18nTwinClassField(), mapperContext.forkOnPoint(FaceTW002Modes.FaceTW0022TwinClassFieldMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceTW002Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceTW002Service.loadAccordionItems(srcCollection.stream().map(PointedFace::getConfig).toList());
    }
}
