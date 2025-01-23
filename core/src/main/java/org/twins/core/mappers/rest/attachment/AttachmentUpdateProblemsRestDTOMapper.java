package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.attachment.AttachmentUpdateProblem;
import org.twins.core.dto.rest.attachment.AttachmentFileUpdateProblemDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentValidateProblemsMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentValidateProblemsMode.class})
public class AttachmentUpdateProblemsRestDTOMapper extends RestSimpleDTOMapper<AttachmentUpdateProblem, AttachmentFileUpdateProblemDTOv1> {

    @Override
    public void map(AttachmentUpdateProblem src, AttachmentFileUpdateProblemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setProblem(src.getProblem())
                .setExternalId(src.getId())
                .setMessage(src.getMessage());
    }
}
