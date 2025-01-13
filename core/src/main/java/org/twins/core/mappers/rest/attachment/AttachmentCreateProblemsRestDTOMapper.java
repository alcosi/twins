package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.attachment.AttachmentCreateProblem;
import org.twins.core.dto.rest.attachment.AttachmentFileCreateProblemDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentValidateProblemsMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentValidateProblemsMode.class})
public class AttachmentCreateProblemsRestDTOMapper extends RestSimpleDTOMapper<AttachmentCreateProblem, AttachmentFileCreateProblemDTOv1> {

    @Override
    public void map(AttachmentCreateProblem src, AttachmentFileCreateProblemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setProblem(src.getProblem())
                .setExternalId(src.getExternalId())
                .setMessage(src.getMessage());
    }
}
