package org.twins.face.mappers.rest.tc.tc001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.tc.tc001.FaceTC001OptionEntity;
import org.twins.face.dto.rest.tc.tc001.FaceTC001OptionDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTC001OptionRestDTOMapper  extends RestSimpleDTOMapper<FaceTC001OptionEntity, FaceTC001OptionDTOv1> {
    private final I18nService i18nService;
    private final FaceTwinPointerService faceTwinPointerService;

    @Override
    public void map(FaceTC001OptionEntity src, FaceTC001OptionDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity headTwin = src.getHeadTwinPointerId() == null ? null : faceTwinPointerService.getPointer(src.getHeadTwinPointerId());
        dst
                .setId(src.getId())
                .setClassSelectorLabel(i18nService.translateToLocale(src.getClassSelectorLabelI18nId()))
                .setTwinClassSearchId(src.getTwinClassSearchId())
                .setPointedHeadTwinId(headTwin == null ? null : headTwin.getId())
                .setClassSelectorLabel(i18nService.translateToLocale(src.getClassSelectorLabelI18nId()))
                .setTwinClassFieldSearchId(src.getTwinClassFieldSearchId())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId()));

    }
}
