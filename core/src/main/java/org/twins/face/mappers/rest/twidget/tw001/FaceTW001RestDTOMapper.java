package org.twins.face.mappers.rest.twidget.tw001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twidget.tw001.FaceTW001Entity;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001DTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW001RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW001Entity>, FaceTW001DTOv1> {

    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final I18nService i18nService;
    @MapperModePointerBinding(modes = FaceTW001Modes.FaceTW0012TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final AttachmentRestrictionService attachmentRestrictionService;

    @Override
    public void map(PointedFace<FaceTW001Entity> src, FaceTW001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> {
                FaceTW001Entity tw001 = src.getConfig();
                TwinAttachmentRestrictionEntity restrictionEntity;

                if (tw001.getImagesTwinClassFieldId() != null) {
                    restrictionEntity = attachmentRestrictionService.getRestrictionEntityFromFieldTyper(tw001.getImagesTwinClassField());
                } else {
                    restrictionEntity = attachmentRestrictionService.getTwinAttachmentRestrictionEntityById(
                            src.getTargetTwin().getTwinClass().getGeneralAttachmentRestrictionId()
                    );
                }

                dst
                        .setKey(tw001.getKey())
                        .setLabel(i18nService.translateToLocale(tw001.getLabelI18nId()))
                        .setImagesTwinClassFieldId(tw001.getImagesTwinClassFieldId())
                        .setUploadable(tw001.isUploadable())
                        .setRestriction(attachmentRestrictionRestDTOMapper.convert(restrictionEntity));
            }
        }
        if (mapperContext.hasModeButNot(FaceTW001Modes.FaceTW0012TwinClassFieldMode.HIDE)) {
            dst.setImagesTwinClassFieldId(src.getConfig().getImagesTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getImagesTwinClassField(), mapperContext.forkOnPoint(FaceTW001Modes.FaceTW0012TwinClassFieldMode.SHORT));
        }
    }
}
