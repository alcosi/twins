package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapper;
import org.twins.core.service.user.UserService;

@Component
@RequiredArgsConstructor
public class TwinUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<Pair<TwinUpdateDTOv1, TwinEntity>, TwinUpdate> {
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final AttachmentCUDRestDTOReverseMapper twinAttachmentCUDRestDTOReverseMapper;
    private final TwinLinkCUDRestDTOReverseMapper twinLinkCUDRestDTOReverseMapper;
    private final UserService userService;

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
            if (twinUpdateDTO.getTagsUpdate() != null) {
                dst
                        .setTagsDelete(twinUpdateDTO.getTagsUpdate().deleteTags())
                        .setExistingTags(twinUpdateDTO.getTagsUpdate().existingTags())
                        .setNewTags(twinUpdateDTO.getTagsUpdate().newTags());
            }
        }
    }
}
