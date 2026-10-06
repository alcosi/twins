package org.twins.core.mappers.rest.link;

import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class LinkCreateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkCreateDTOv1, LinkEntity> {

    @Override
    public void map(LinkCreateDTOv1 src, LinkEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setType(src.getType())
                .setLinkStrengthId(src.getLinkStrength())
                .setLinkerFeaturerId(src.getLinkerFeaturerId())
                .setLinkerParams(src.getLinkerParams())
                .setSrcTwinClassInheritable(src.getSrcTwinClassInheritable())
                .setDstTwinClassInheritable(src.getDstTwinClassInheritable())
                .setRelationTwinClassId(src.getRelationTwinClassId())
                .setSrcTwinClassId(src.getSrcTwinClassId())
                .setDstTwinClassId(src.getDstTwinClassId());
    }
}
