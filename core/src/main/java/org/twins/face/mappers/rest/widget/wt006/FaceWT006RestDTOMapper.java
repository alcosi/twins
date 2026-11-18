package org.twins.face.mappers.rest.widget.wt006;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.widget.wt006.FaceWT006Entity;
import org.twins.face.domain.widget.wt006.FaceWT006TwinClassField;
import org.twins.face.dto.rest.widget.wt006.FaceWT006DTOv1;
import org.twins.face.service.widget.FaceWT006Service;

import java.util.List;

@Deprecated
@Component
@RequiredArgsConstructor
public class FaceWT006RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT006Entity>, FaceWT006DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT006Entity> faceWidgetRestDTOMapper;
    private final FaceWT006Service faceWT006Service;
    @MapperModePointerBinding(modes = FaceWT006Modes.FaceWT0062TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT006Entity> src, FaceWT006DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        List<FaceWT006TwinClassField> fields = faceWT006Service.loadFields(src);
        if (CollectionUtils.isEmpty(fields)) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "no field configured for widget");
        }
        FaceWT006TwinClassField faceWT006TwinClassField = fields.getFirst();
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(faceWT006TwinClassField.getField().getKey())
                    .setEditable(true);
            case DETAILED -> dst
                    .setKey(faceWT006TwinClassField.getField().getKey())
                    .setLabel(I18nCacheHolder.addId(faceWT006TwinClassField.getField().getNameI18nId()))
                    .setTwinClassFieldId(faceWT006TwinClassField.getField().getId())
                    .setEditable(faceWT006TwinClassField.isEditable());
        }
        if (mapperContext.hasModeButNot(FaceWT006Modes.FaceWT0062TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(faceWT006TwinClassField.getField().getId());
            twinClassFieldRestDTOMapper.postpone(faceWT006TwinClassField.getField(), mapperContext.forkOnPoint(FaceWT006Modes.FaceWT0062TwinClassFieldMode.SHORT));
        }
    }
}
