package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentRestrictionCreateDTOReverseMapper extends RestSimpleDTOMapper<AttachmentRestrictionCreateDTOv1, TwinAttachmentRestrictionEntity> {
    private final AttachmentRestrictionSaveDTOReverseMapper attachmentRestrictionSaveDTOReverseMapper;

    @Override
    public void map(AttachmentRestrictionCreateDTOv1 src, TwinAttachmentRestrictionEntity dst, MapperContext mapperContext) throws Exception {
        attachmentRestrictionSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
