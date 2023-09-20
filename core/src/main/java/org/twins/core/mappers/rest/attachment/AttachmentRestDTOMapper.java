package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class AttachmentRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setAuthorUser(userDTOMapper.convert(src.getCreatedByUser(), mapperProperties.setModeIfNotPresent(UserDTOMapper.Mode.ID_ONLY)))
                        .setTwinId(src.getTwinId())
                        .setCreatedAt(src.getCreatedAt().toInstant())
                        .setDescription(src.getDescription())
                        .setTitle(src.getTitle())
                        .setExternalId(src.getExternalId());
            case ID_LINK_ONLY:
                dst
                        .setId(src.getId())
                        .setStorageLink(src.getStorageLink());
        }
    }

    public enum Mode implements MapperMode {
        ID_LINK_ONLY, DETAILED;

        public static final String _ID_LINK_ONLY = "ID_LINK_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
