package org.twins.core.mappers.rest.attachment;

import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class AttachmentRestrictionCreateDTOReverseMapper extends RestSimpleDTOMapper<AttachmentRestrictionCreateDTOv1, TwinAttachmentRestrictionEntity> {
    @Override
    public void map(AttachmentRestrictionCreateDTOv1 src, TwinAttachmentRestrictionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setMinCount(src.getMinCount())
                .setMaxCount(src.getMaxCount())
                .setFileSizeMbLimit(src.getFileSizeMbLimit())
                .setFileExtensionLimit(src.getFileExtensionLimit())
                .setFileNameRegexp(src.getFileNameRegexp());
    }
}
