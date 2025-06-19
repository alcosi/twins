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
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.domain.tc.tc001.FaceTC001Twin;
import org.twins.face.dto.rest.tc.tc001.FaceTC001DTOv1;
import org.twins.face.service.tc.FaceTC001Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class FaceTC001RestDTOMapper extends RestSimpleDTOMapper<FaceTC001Twin, FaceTC001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceTC001Service faceTC001Service;
    private final FaceTC001FieldRestDTOMapper faceTC001FieldRestDTOMapper;

    @MapperModePointerBinding(modes = FaceTC001Modes.FaceTC0012TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceTC001Twin src, FaceTC001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getEntity().getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getEntity().getKey());
            case DETAILED -> {
                faceTC001Service.loadFields(src.getEntity());
                TwinEntity headTwin = faceTC001Service.getHeadTwin(src.getTwinId(), src.getEntity());
                dst
                        .setKey(src.getEntity().getKey())
                        .setStyleClasses(StringUtils.splitToSet(src.getEntity().getStyleClasses(), " "))
                        .setHeader(i18nService.translateToLocale(src.getEntity().getHeaderI18nId() != null ? src.getEntity().getHeaderI18nId() : src.getEntity().getTwinClass().getNameI18NId()))
                        .setIcon(resourceService.getResourceUri(src.getEntity().getIconResource()))
                        .setTwinClassId(src.getEntity().getTwinClassId())
                        .setExtendsDepth(src.getEntity().getExtendsDepth())
                        .setPointedHeadTwinId(headTwin == null ? null : headTwin.getId())
                        .setFields(faceTC001FieldRestDTOMapper.convertCollection(src.getEntity().getFields()));

                if (mapperContext.hasModeButNot(FaceTC001Modes.FaceTC0012TwinClassMode.HIDE)) {
                    twinClassRestDTOMapper.postpone(src.getEntity().getTwinClass(), mapperContext.forkOnPoint(FaceTC001Modes.FaceTC0012TwinClassMode.SHORT));
                }
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceTC001Twin> srcCollection, MapperContext mapperContext) throws ServiceException {
        List<FaceTC001Entity> entities = srcCollection.stream().map(FaceTC001Twin::getEntity).toList();
        faceTC001Service.loadFields(entities);
    }
}
