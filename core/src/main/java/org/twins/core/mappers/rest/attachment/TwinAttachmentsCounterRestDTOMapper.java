package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentCountMode;
import org.twins.core.service.twin.TwinAttachmentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinAttachmentsCounterRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, AttachmentsCountDTOv1> {

    private final TwinAttachmentService twinAttachmentService;

    @Override
    public void map(TwinEntity src, AttachmentsCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentCountMode.Twin2AttachmentCountMode.SHORT)) {
            case SHORT:
                if (src.getAttachmentsCount() != null)
                    dst.setAll(src.getAttachmentsCount().getAll());
            case DETAILED:
                if (src.getAttachmentsCount() != null) {
                    dst
                            .setDirect(src.getAttachmentsCount().getDirect())
                            .setFromComments(src.getAttachmentsCount().getFromComments())
                            .setFromFields(src.getAttachmentsCount().getFromFields())
                            .setFromTransitions(src.getAttachmentsCount().getFromTransitions());
                }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(AttachmentCountMode.Twin2AttachmentCountMode.SHORT))
            twinAttachmentService.loadAttachmentsCount(srcCollection, true);
        else if (mapperContext.hasMode(AttachmentCountMode.Twin2AttachmentCountMode.DETAILED))
            twinAttachmentService.loadAttachmentsCount(srcCollection, false);
    }
}
