package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentRestrictionSaveDTOReverseMapper extends RestSimpleDTOMapper<AttachmentRestrictionSaveDTOv1, TwinAttachmentRestrictionEntity> {
    @Override
    public void map(AttachmentRestrictionSaveDTOv1 src, TwinAttachmentRestrictionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setMinCount(src.getMinCount())
                .setMaxCount(src.getMaxCount())
                .setFileSizeMbLimit(src.getFileSizeMbLimit())
                .setFileExtensionLimit(src.getFileExtensionLimit())
                .setFileNameRegexp(src.getFileNameRegexp());
    }
}
