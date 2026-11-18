package org.twins.face.mappers.rest.widget.wt005;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.widget.wt005.FaceWT005Entity;
import org.twins.face.dto.rest.widget.wt005.FaceWT005DTOv1;
import org.twins.face.service.widget.FaceWT005Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceWT005RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT005Entity>, FaceWT005DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT005Entity> faceWidgetRestDTOMapper;
    private final FaceWT005Service faceWT005Service;
    private final FaceWT005AccordionItemRestDTOMapper faceWT005AccordionItemRestDTOMapper;
    @MapperModePointerBinding(modes = FaceWT005Modes.FaceWT0052TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;


    @Override
    public void map(PointedFace<FaceWT005Entity> src, FaceWT005DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> dst
                    .setKey(src.getConfig().getKey())
                    .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelI18nId() != null ?
                            src.getConfig().getLabelI18nId() : src.getConfig().getI18nTwinClassField().getNameI18nId()))
                    .setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
        }
        faceWT005Service.loadAccordionItems(src.getConfig());
        dst.setAccordionItems(faceWT005AccordionItemRestDTOMapper.convertCollection(src.getConfig().getAccordionItems(), mapperContext));
        if (mapperContext.hasModeButNot(FaceWT005Modes.FaceWT0052TwinClassFieldMode.HIDE)) {
            dst.setI18nTwinClassFieldId(src.getConfig().getI18nTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getI18nTwinClassField(), mapperContext.forkOnPoint(FaceWT005Modes.FaceWT0052TwinClassFieldMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT005Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT005Service.loadAccordionItems(srcCollection.stream().map(PointedFace::getConfig).toList());
    }
}
