package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.dto.rest.twin.TwinBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
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
        switch (mapperContext.getModeOrUse(AttachmentsMode.HIDE)) {
            case HIDE:
                break;
            case SHOW:
                dst.attachments(attachmentRestDTOMapper.convertList(attachmentService.findAttachmentByTwinId(src.getId()), mapperContext));
                break;
        }
//        switch (mapperContext.getModeOrUse(LinkMode.HIDE)) {
//            case HIDE:
//                break;
//            case SHOW:
//                dst.links(twinLinkListRestDTOMapper.convert(twinLinkService.findTwinLinks(src.getId()), mapperContext));
//                break;
//        }
        dst.links(twinLinkListRestDTOMapper.convert(twinLinkService.findTwinLinks(src.getId()), mapperContext));
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    public enum AttachmentsMode implements MapperMode {
        SHOW, HIDE;

        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }

    public enum LinkMode implements MapperMode {
        SHOW, HIDE;

        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }
}
