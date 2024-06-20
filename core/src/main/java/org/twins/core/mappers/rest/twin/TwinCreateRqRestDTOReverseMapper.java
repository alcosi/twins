package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.dto.rest.twin.TwinTagAddDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.HashSet;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TwinCreateRqRestDTOReverseMapper extends RestSimpleDTOMapper<TwinCreateRqDTOv2, TwinCreate> {
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;
    final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    final TwinService twinService;
    final UserService userService;
    final AuthService authService;

    @Override
    public void map(TwinCreateRqDTOv2 src, TwinCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setTwinEntity(new TwinEntity()
                        .setTwinClassId(src.getClassId())
                        .setName(src.getName() == null ? "" : src.getName())
                        .setCreatedByUserId(apiUser.getUser().getId())
                        .setCreatedByUser(apiUser.getUser())
                        .setHeadTwinId(src.getHeadTwinId())
                        .setAssignerUserId(userService.checkId(src.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                        .setDescription(src.getDescription()))
                .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(src.getClassId(), src.getFields()));
        dst
                .setAttachmentEntityList(attachmentAddRestDTOReverseMapper.convertList(src.getAttachments()))
                .setLinksEntityList(twinLinkAddRestDTOReverseMapper.convertList(src.getLinks()))
                .setNewTags(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::newTags)
                        .orElseGet(HashSet::new))
                .setExistingTags(Optional.ofNullable(src.getTags())
                        .map(TwinTagAddDTOv1::existingTags)
                        .orElseGet(HashSet::new));
    }
}
