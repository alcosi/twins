package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinBaseRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv1> {
    @Override
    public void map(TwinEntity src, TwinBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinMode.ID_NAME_ONLY)) {
            case DETAILED:
                dst
                        .assignerUserId(src.getAssignerUserId())
                        .authorUserId(src.getCreatedByUserId())
                        .statusId(src.getTwinStatusId())
                        .twinClassId(src.getTwinClassId())
                        .description(src.getDescription())
                        .createdAt(src.getCreatedAt().toInstant());
            case ID_NAME_ONLY:
                dst
                        .id(src.getId())
                        .name(src.getName());
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    public enum TwinMode implements MapperMode {
        ID_NAME_ONLY, DETAILED;

        public static final String _ID_NAME_ONLY = "ID_NAME_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
