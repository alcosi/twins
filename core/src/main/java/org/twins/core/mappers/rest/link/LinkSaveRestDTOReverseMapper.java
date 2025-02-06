package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class LinkSaveRestDTOReverseMapper extends RestSimpleDTOMapper<LinkSaveDTOv1, LinkEntity> {


    @Override
    public void map(LinkSaveDTOv1 src, LinkEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setType(src.getType())
                .setLinkStrengthId(src.getLinkStrength())
                .setLinkerFeaturerId(src.getLinkerFeaturerId())
                .setLinkerParams(src.getLinkerParams());
    }
}
