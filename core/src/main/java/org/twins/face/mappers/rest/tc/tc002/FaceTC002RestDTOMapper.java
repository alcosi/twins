package org.twins.face.mappers.rest.tc.tc002;

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
import org.twins.face.dao.tc.tc002.FaceTC002Entity;
import org.twins.face.dto.rest.tc.tc002.FaceTC002DTOv1;
import org.twins.face.mappers.rest.tc.tc001.FaceTC001Modes;
import org.twins.face.service.tc.FaceTC002Service;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceTC002RestDTOMapper extends RestSimpleDTOMapper<FaceTC002Entity, FaceTC002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceTC002Service faceTC002Service;
    private final FaceTwinPointerService faceTwinPointerService;
    private final FaceTC002FieldRestDTOMapper faceTC002FieldRestDTOMapper;

    @MapperModePointerBinding(modes = FaceTC002Modes.FaceTC0022TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceTC002Entity src, FaceTC002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                if (mapperContext.hasModeButNot(FaceTC001Modes.FaceTC0012TwinClassMode.HIDE)) {
                    faceTC002Service.loadFields(src);
                    twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(FaceTC001Modes.FaceTC0012TwinClassMode.SHORT));
                }
                TwinEntity headTwin = faceTwinPointerService.getPointer(src.getHeadTwinPointerId());
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
                        .setFields(faceTC002FieldRestDTOMapper.convertCollection(src.getFields()));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceTC002Entity> srcCollection, MapperContext mapperContext) throws SecurityException, ServiceException {
        if (mapperContext.hasModeButNot(FaceTC002Modes.FaceTC0022TwinClassMode.HIDE)) {
            faceTC002Service.loadFields(srcCollection);
        }
    }
}
