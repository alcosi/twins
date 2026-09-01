package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentRestrictionUpdateDTOReverseMapper extends RestSimpleDTOMapper<AttachmentRestrictionUpdateDTOv1, TwinAttachmentRestrictionEntity> {
    private final AttachmentRestrictionSaveDTOReverseMapper attachmentRestrictionSaveDTOReverseMapper;

    @Override
    public void map(AttachmentRestrictionUpdateDTOv1 src, TwinAttachmentRestrictionEntity dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        attachmentRestrictionSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
