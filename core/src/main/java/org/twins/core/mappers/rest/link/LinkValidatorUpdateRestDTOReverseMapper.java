package org.twins.core.mappers.rest.link;

import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.domain.link.LinkValidatorUpdate;
import org.twins.core.dto.rest.link.LinkValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class LinkValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkValidatorUpdateDTOv1, LinkValidatorUpdate> {

    @Override
    public void map(LinkValidatorUpdateDTOv1 src, LinkValidatorUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setLinkValidator(
                new LinkValidatorEntity()
                        .setLinkId(src.getLinkId())
                        .setLinkerFeaturerId(src.getLinkerFeaturerId())
                        .setLinkerParams(src.getLinkerParams())
                        .setOrder(src.getOrder())
        );
        dst.setId(src.getId());
    }
}
