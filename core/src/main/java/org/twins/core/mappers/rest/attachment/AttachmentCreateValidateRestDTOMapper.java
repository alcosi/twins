package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentCreateValidateRestDTOMapper extends RestSimpleDTOMapper<AttachmentCUDValidateResult, AttachmentCreateValidateRsDTOv1> {

    private final AttachmentProblemsRestDTOMapper attachmentProblemsRestDTOMapper;

    @Override
    public void map(AttachmentCUDValidateResult src, AttachmentCreateValidateRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setCudProblems(attachmentProblemsRestDTOMapper.convert(src.getCudProblems(), mapperContext));
    }
}
