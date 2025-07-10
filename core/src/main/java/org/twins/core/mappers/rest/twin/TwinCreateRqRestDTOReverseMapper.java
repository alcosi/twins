package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.dto.rest.twin.TwinTagAddDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.util.HashSet;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TwinCreateRqRestDTOReverseMapper extends RestSimpleDTOMapper<TwinCreateRqDTOv2, TwinCreate> {

    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;

    private final UserService userService;
    private final AuthService authService;

    @Override
    public void map(TwinCreateRqDTOv2 src, TwinCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(src.getClassId(), src.getFields()))
                .setTwinEntity(new TwinEntity()
                        .setTwinClassId(src.getClassId())
                        .setName(src.getName() == null ? "" : src.getName())
                        .setCreatedByUserId(apiUser.getUser().getId())
                        .setCreatedByUser(apiUser.getUser())
                        .setHeadTwinId(src.getHeadTwinId())
                        .setAssignerUserId(userService.checkId(src.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                        .setDescription(src.getDescription())
                        .setExternalId(src.getExternalId()));
        dst
                .setAttachmentEntityList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getAttachments()))
                .setLinksEntityList(twinLinkAddRestDTOReverseMapper.convertCollection(src.getLinks()))
                .setTagsAddNew(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::newTags)
                        .orElseGet(HashSet::new))
                .setTagsAddExisted(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::existingTags)
                        .orElseGet(HashSet::new));
    }
}
