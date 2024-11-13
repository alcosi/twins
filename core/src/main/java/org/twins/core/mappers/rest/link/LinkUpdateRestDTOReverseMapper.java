package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.dto.rest.link.LinkUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class LinkUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<LinkUpdateDTOv1, LinkUpdate> {

    private final LinkSaveRestDTOReverseMapper linkSaveRestDTOReverseMapper;

    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;

    @Override
    public void map(LinkUpdateDTOv1 src, LinkUpdate dst, MapperContext mapperContext) throws Exception {
        linkSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setSrcTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getSrcTwinClassUpdate()))
                .setDstTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getDstTwinClassUpdate()));
    }
}
