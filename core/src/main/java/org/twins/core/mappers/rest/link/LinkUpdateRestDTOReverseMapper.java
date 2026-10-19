package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.dto.rest.link.LinkUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class LinkUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkUpdateDTOv1, LinkUpdate> {

    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;

    @Override
    public void map(LinkUpdateDTOv1 src, LinkUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setType(src.getType())
                .setLinkStrengthId(src.getLinkStrength())
                .setLinkerFeaturerId(src.getLinkerFeaturerId())
                .setLinkerParams(src.getLinkerParams())
                .setSrcTwinClassInheritable(src.getSrcTwinClassInheritable())
                .setDstTwinClassInheritable(src.getDstTwinClassInheritable())
                .setRelationTwinClassId(src.getRelationTwinClassId());
        dst
                .setSrcTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getSrcTwinClassUpdate()))
                .setDstTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getDstTwinClassUpdate()))
                .setSrcTwinClassInheritable(src.getSrcTwinClassInheritable())
                .setDstTwinClassInheritable(src.getDstTwinClassInheritable());
    }
}
