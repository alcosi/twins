package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentRestrictionRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentRestrictionEntity, AttachmentRestrictionDTOv1> {

    @Override
    public void map(TwinAttachmentRestrictionEntity src, AttachmentRestrictionDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src != null) {
            dst
                    .setMinCount(src.getMinCount())
                    .setMaxCount(src.getMaxCount())
                    .setFileSizeMbLimit(src.getFileSizeMbLimit())
                    .setFileExtensionLimit(src.getFileExtensionLimit())
                    .setFileNameRegexp(src.getFileNameRegexp());
        }
    }
}
