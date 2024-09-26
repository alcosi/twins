package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinAttachmentCountMode;
import org.twins.core.service.attachment.AttachmentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinAttachmentsCounterRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, AttachmentsCountDTOv1> {

    private final AttachmentService attachmentService;

    @Override
    public void map(TwinEntity src, AttachmentsCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        attachmentService.loadAttachmentsCount(src);
        switch (mapperContext.getModeOrUse(TwinAttachmentCountMode.DETAILED)) {
            case DETAILED:
                dst
                        .setDirect(src.getTwinAttachmentsCount().getDirect())
                        .setFromComments(src.getTwinAttachmentsCount().getFromComments())
                        .setFromFields(src.getTwinAttachmentsCount().getFromFields())
                        .setFromTransitions(src.getTwinAttachmentsCount().getFromTransitions());
            case SHORT:
                dst.setAll(src.getTwinAttachmentsCount().getAll());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        attachmentService.loadAttachmentsCount(srcCollection);
    }
}
