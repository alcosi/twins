package org.twins.face.mappers.rest.twidget.tw001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.twidget.tw001.FaceTW001Entity;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001DTOv1;
import org.twins.face.service.twidget.FaceTW001Service;


@Component
@RequiredArgsConstructor
public class FaceTW001RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW001Entity>, FaceTW001DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    @MapperModePointerBinding(modes = FaceTW001Modes.FaceTW0012TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final FaceTW001Service faceTW001Service;
    @MapperModePointerBinding(modes = FaceTW001Modes.FaceTW0012AttachmentRestrictionMode.class)
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;

    @Override
    public void map(PointedFace<FaceTW001Entity> src, FaceTW001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> {
                FaceTW001Entity tw001 = src.getConfig();
                faceTW001Service.loadRestriction(src);

                dst
                        .setKey(tw001.getKey())
                        .setLabel(I18nCacheHolder.addId(tw001.getLabelI18nId()))
                        .setImagesTwinClassFieldId(tw001.getImagesTwinClassFieldId())
                        .setUploadable(tw001.isUploadable())
                        .setRestrictionId(tw001.getTwinAttachmentRestriction() != null ? tw001.getTwinAttachmentRestriction().getId() : null);

            }
        }

        if (mapperContext.hasModeButNot(FaceTW001Modes.FaceTW0012TwinClassFieldMode.HIDE)) {
            dst.setImagesTwinClassFieldId(src.getConfig().getImagesTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getImagesTwinClassField(), mapperContext.forkOnPoint(FaceTW001Modes.FaceTW0012TwinClassFieldMode.SHORT));
        }

        if (mapperContext.hasModeButNot(FaceTW001Modes.FaceTW0012AttachmentRestrictionMode.HIDE)) {
            faceTW001Service.loadRestriction(src);

            if (src.getConfig().getTwinAttachmentRestriction() != null) {
                dst.setRestrictionId(src.getConfig().getTwinAttachmentRestriction().getId());
                attachmentRestrictionRestDTOMapper.postpone(src.getConfig().getTwinAttachmentRestriction(), mapperContext.forkOnPoint(FaceTW001Modes.FaceTW0012AttachmentRestrictionMode.SHOW));
            }
        }
    }
}
