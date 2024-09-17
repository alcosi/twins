package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.attachment.AttachmentsCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentCountMode;

@Component
@RequiredArgsConstructor
public class TwinAttachmentsCounterRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, AttachmentsCountDTOv1> {

    @Override
    public void map(TwinEntity src, AttachmentsCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentCountMode.DETAILED)) {
            case DETAILED:
                dst
                        .setDirect(src.getAttachmentsCount().getDirect())
                        .setFromComments(src.getAttachmentsCount().getFromComments())
                        .setFromFields(src.getAttachmentsCount().getFromFields())
                        .setFromTransitions(src.getAttachmentsCount().getFromTransitions());
            case SHORT:
                dst
                        .setAll(src.getAttachmentsCount().getAll());
        }
    }
}
