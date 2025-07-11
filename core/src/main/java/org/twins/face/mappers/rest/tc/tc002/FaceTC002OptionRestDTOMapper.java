package org.twins.face.mappers.rest.tc.tc002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.tc.tc002.FaceTC002OptionEntity;
import org.twins.face.dto.rest.tc.tc002.FaceTC002OptionDTOv1;
import org.twins.face.service.tc.FaceTC002OptionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceTC002OptionRestDTOMapper extends RestSimpleDTOMapper<FaceTC002OptionEntity, FaceTC002OptionDTOv1> {
    private final FaceTC002OptionService faceTC002OptionService;
    private final I18nService i18nService;
    private final FaceTwinPointerService faceTwinPointerService;

    @MapperModePointerBinding(modes = FaceTC002Modes.FaceTC0022TwinClassFieldMode.class)
    private final FaceTC002FieldRestDTOMapper faceTC002FieldRestDTOMapper;

    @MapperModePointerBinding(modes = FaceTC002Modes.FaceTC0022TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceTC002OptionEntity src, FaceTC002OptionDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity headTwin = faceTwinPointerService.getPointer(src.getHeadTwinPointerId());
        if (mapperContext.hasModeButNot(FaceTC002Modes.FaceTC0022TwinClassFieldMode.HIDE)){
            faceTC002OptionService.loadFields(src);
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(FaceTC002Modes.FaceTC0022TwinClassFieldMode.SHORT));
        }

        dst
                .setId(src.getId())
                .setClassSelectorLabel(i18nService.translateToLocale(src.getClassSelectorLabelI18nId()))
                .setTwinClassId(src.getTwinClassId())
                .setExtendsDepth(src.getExtendsDepth())
                .setPointedHeadTwinId(headTwin == null ? null : headTwin.getId())
                .setFields(faceTC002FieldRestDTOMapper.convertCollection(src.getFields()));
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceTC002OptionEntity> srcCollection, MapperContext mapperContext) throws SecurityException, ServiceException {
        if (mapperContext.hasModeButNot(FaceTC002Modes.FaceTC0022TwinClassMode.HIDE)) {
            faceTC002OptionService.loadFields(srcCollection);
        }
    }
}
