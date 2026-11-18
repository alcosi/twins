package org.twins.face.mappers.rest.widget.wt004;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.widget.wt004.FaceWT004Entity;
import org.twins.face.dto.rest.widget.wt004.FaceWT004DTOv1;
import org.twins.face.service.widget.FaceWT004Service;


@Component
@RequiredArgsConstructor
public class FaceWT004RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT004Entity>, FaceWT004DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT004Entity> faceWidgetRestDTOMapper;
    @MapperModePointerBinding(modes = FaceWT004Modes.FaceWT0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final FaceWT004Service faceWT004Service;
    @MapperModePointerBinding(modes = FaceWT004Modes.FaceWT0042AttachmentRestrictionMode.class)
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT004Entity> src, FaceWT004DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> {
                FaceWT004Entity tw001 = src.getConfig();
                faceWT004Service.loadRestriction(src);

                dst
                        .setKey(tw001.getKey())
                        .setLabel(I18nCacheHolder.addId(tw001.getLabelI18nId()))
                        .setImagesTwinClassFieldId(tw001.getImagesTwinClassFieldId())
                        .setUploadable(tw001.isUploadable())
                        .setRestrictionId(tw001.getTwinAttachmentRestriction() != null ? tw001.getTwinAttachmentRestriction().getId() : null);

            }
        }

        if (mapperContext.hasModeButNot(FaceWT004Modes.FaceWT0042TwinClassFieldMode.HIDE)) {
            dst.setImagesTwinClassFieldId(src.getConfig().getImagesTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getImagesTwinClassField(), mapperContext.forkOnPoint(FaceWT004Modes.FaceWT0042TwinClassFieldMode.SHORT));
        }

        if (mapperContext.hasModeButNot(FaceWT004Modes.FaceWT0042AttachmentRestrictionMode.HIDE)) {
            faceWT004Service.loadRestriction(src);

            if (src.getConfig().getTwinAttachmentRestriction() != null) {
                dst.setRestrictionId(src.getConfig().getTwinAttachmentRestriction().getId());
                attachmentRestrictionRestDTOMapper.postpone(src.getConfig().getTwinAttachmentRestriction(), mapperContext.forkOnPoint(FaceWT004Modes.FaceWT0042AttachmentRestrictionMode.SHOW));
            }
        }
    }
}
