package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class LinkCreateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkCreateDTOv1, LinkEntity> {

    private final LinkSaveRestDTOReverseMapper linkSaveRestDTOReverseMapper;

    @Override
    public void map(LinkCreateDTOv1 src, LinkEntity dst, MapperContext mapperContext) throws Exception {
        linkSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setSrcTwinClassId(src.getSrcTwinClassId())
                .setDstTwinClassId(src.getDstTwinClassId());
    }
}
