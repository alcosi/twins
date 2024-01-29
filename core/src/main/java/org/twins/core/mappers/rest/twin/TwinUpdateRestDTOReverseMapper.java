package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.dto.rest.twin.TwinTagAddDTOv1;
import org.twins.core.dto.rest.twin.TwinTagManageDTOv1;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapper;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.HashSet;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TwinUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<Pair<TwinUpdateDTOv1, TwinEntity>, TwinUpdate> {
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final AttachmentCUDRestDTOReverseMapper twinAttachmentCUDRestDTOReverseMapper;
    final TwinLinkCUDRestDTOReverseMapper twinLinkCUDRestDTOReverseMapper;
    final TwinService twinService;
    final UserService userService;

    @Override
    public void map(Pair<TwinUpdateDTOv1, TwinEntity> src, TwinUpdate dst, MapperContext mapperContext) throws Exception {
        TwinUpdateDTOv1 twinUpdateDTO = src.getLeft();
        TwinEntity dbTwinEntity = src.getRight();
        dst.setDbTwinEntity(dbTwinEntity);
        if (twinUpdateDTO != null) {
            TwinEntity updatedTwinEntity = new TwinEntity()
                    .setId(dbTwinEntity.getId())
                    .setName(twinUpdateDTO.getName())
                    .setHeadTwinId(twinUpdateDTO.getHeadTwinId())
                    .setDescription(twinUpdateDTO.getDescription());
            dst
                    .setTwinEntity(updatedTwinEntity)
                    .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(dbTwinEntity.getTwinClassId(), twinUpdateDTO.getFields()));
            dst
                    .setAttachmentCUD(twinAttachmentCUDRestDTOReverseMapper.convert(twinUpdateDTO))
                    .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapper.convert(twinUpdateDTO));
            if (twinUpdateDTO.getAssignerUserId() != null) {
                UserEntity newAssignee = userService.loadUserAndCheck(twinUpdateDTO.getAssignerUserId());
                updatedTwinEntity
                        .setAssignerUser(newAssignee)
                        .setAssignerUserId(newAssignee.getId());
            }

            // map tags to update
            dst
                    .setTagsDelete(Optional.ofNullable(twinUpdateDTO.getTagsUpdate())
                            .map(TwinTagManageDTOv1::deleteTags)
                            .orElseGet(HashSet::new))
                    .setExistingTags(Optional.ofNullable(twinUpdateDTO.getTagsUpdate())
                            .map(TwinTagAddDTOv1::existingTags)
                            .orElseGet(HashSet::new))
                    .setNewTags(Optional.ofNullable(twinUpdateDTO.getTagsUpdate())
                            .map(TwinTagAddDTOv1::newTags)
                            .orElseGet(HashSet::new));
        }
    }
}
