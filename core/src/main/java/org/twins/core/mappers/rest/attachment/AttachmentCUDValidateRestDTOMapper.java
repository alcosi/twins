package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentCUDValidateRestDTOMapper extends RestSimpleDTOMapper<AttachmentCUDValidateResult, AttachmentCUDValidateRsDTOv1> {

    @Override
    public void map(AttachmentCUDValidateResult src, AttachmentCUDValidateRsDTOv1 dst, MapperContext mapperContext) throws Exception {

    }
}
