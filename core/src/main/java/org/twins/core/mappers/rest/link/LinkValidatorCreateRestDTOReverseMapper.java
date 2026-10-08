package org.twins.core.mappers.rest.link;

import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.domain.link.LinkValidatorCreate;
import org.twins.core.dto.rest.link.LinkValidatorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class LinkValidatorCreateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkValidatorCreateDTOv1, LinkValidatorCreate> {

    @Override
    public void map(LinkValidatorCreateDTOv1 src, LinkValidatorCreate dst, MapperContext mapperContext) throws Exception {
        dst.setLinkValidator(
                new LinkValidatorEntity()
                        .setLinkId(src.getLinkId())
                        .setLinkerFeaturerId(src.getLinkerFeaturerId())
                        .setLinkerParams(src.getLinkerParams())
                        .setOrder(src.getOrder())
        );
    }
}
