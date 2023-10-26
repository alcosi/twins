package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;


@Component
@RequiredArgsConstructor
public class TwinBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv3> {
    final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final AttachmentService attachmentService;
    final TwinLinkService twinLinkService;
    final TwinLinkListRestDTOMapper twinLinkListRestDTOMapper;


    @Override
    public void map(TwinEntity src, TwinBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            dst.attachments(attachmentRestDTOMapper.convertList(attachmentService.findAttachmentByTwinId(src.getId()), mapperContext));
        if (!twinLinkListRestDTOMapper.hideMode(mapperContext))
            dst.links(twinLinkListRestDTOMapper.convert(twinLinkService.findTwinLinks(src.getId()), mapperContext));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinBaseV2RestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
