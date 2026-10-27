package org.twins.core.mappers.rest.link;

import org.springframework.stereotype.Component;
import org.twins.core.domain.link.LinkValidatorDuplicate;
import org.twins.core.dto.rest.link.LinkValidatorDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class LinkValidatorDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkValidatorDuplicateDTOv1, LinkValidatorDuplicate> {

    @Override
    public void map(LinkValidatorDuplicateDTOv1 src, LinkValidatorDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalLinkValidatorId())
                .setNewParentEntityId(src.getNewLinkId());
    }
}
