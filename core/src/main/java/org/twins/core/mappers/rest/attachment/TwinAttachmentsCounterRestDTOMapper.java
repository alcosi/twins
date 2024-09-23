package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentCountMode;
import org.twins.core.service.attachment.AttachmentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinAttachmentsCounterRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, AttachmentsCountDTOv1> {

    private final AttachmentService attachmentService;

    @Override
    public void map(TwinEntity src, AttachmentsCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        attachmentService.loadAttachmentsCount(src);
        switch (mapperContext.getModeOrUse(AttachmentCountMode.SHORT)) {
            case SHORT:
                dst.setAll(src.getAttachmentsCount().getAll());
            case DETAILED:
                dst
                        .setDirect(src.getAttachmentsCount().getDirect())
                        .setFromComments(src.getAttachmentsCount().getFromComments())
                        .setFromFields(src.getAttachmentsCount().getFromFields())
                        .setFromTransitions(src.getAttachmentsCount().getFromTransitions());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        attachmentService.loadAttachmentsCount(srcCollection);
    }
}
