package org.twins.face.mappers.rest.tc.tc001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.dto.rest.tc.tc001.FaceTC001DTOv1;
import org.twins.face.service.tc.FaceTC001Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceTC001RestDTOMapper extends RestSimpleDTOMapper<FaceTC001Entity, FaceTC001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceTC001Service faceTC001Service;
    private final FaceTwinPointerService faceTwinPointerService;
    private final FaceTC001FieldRestDTOMapper faceTC001FieldRestDTOMapper;

    @MapperModePointerBinding(modes = FaceTC001Modes.FaceTC0012TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceTC001Entity src, FaceTC001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                if (mapperContext.hasModeButNot(FaceTC001Modes.FaceTC0012TwinClassMode.HIDE)) {
                    faceTC001Service.loadFields(src);
                    twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(FaceTC001Modes.FaceTC0012TwinClassMode.SHORT));
                }

                TwinEntity headTwin = src.getHeadTwinPointerId() == null ? null : faceTwinPointerService.getPointer(src.getHeadTwinPointerId());

                dst
                        .setKey(src.getKey())
                        .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                        .setSaveButtonLabel(i18nService.translateToLocale(src.getSaveButtonLabelI18nId()))
                        .setClassSelectorLabel(i18nService.translateToLocale(src.getClassSelectorLabelI18nId()))
                        .setHeader(i18nService.translateToLocale(src.getHeaderI18nId() != null ? src.getHeaderI18nId() : src.getTwinClass().getNameI18NId()))
                        .setIcon(resourceService.getResourceUri(src.getIconResource()))
                        .setTwinClassId(src.getTwinClassId())
                        .setExtendsDepth(src.getExtendsDepth())
                        .setPointedHeadTwinId(headTwin == null ? null : headTwin.getId())
                        .setFields(faceTC001FieldRestDTOMapper.convertCollection(src.getFields()));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceTC001Entity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(FaceTC001Modes.FaceTC0012TwinClassMode.HIDE)) {
            faceTC001Service.loadFields(srcCollection);
        }
    }
}
