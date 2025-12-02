package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.user.UserService;

@Component
@RequiredArgsConstructor
public class TwinUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<Pair<TwinUpdateDTOv1, TwinEntity>, TwinUpdate> {
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final AttachmentCUDRestDTOReverseMapper twinAttachmentCUDRestDTOReverseMapper;
    private final TwinLinkCUDRestDTOReverseMapper twinLinkCUDRestDTOReverseMapper;
    private final TwinFieldAttributeCUDRestDTOReverseMapper twinFieldAttributeCUDRestDTOReverseMapper;
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
                    .setExternalId(twinUpdateDTO.getExternalId())
                    .setDescription(twinUpdateDTO.getDescription());
            dst
                    .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(dbTwinEntity.getTwinClassId(), twinUpdateDTO.getFields()))
                    .setTwinEntity(updatedTwinEntity);
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
                        .setTagsAddExisted(twinUpdateDTO.getTagsUpdate().existingTags())
                        .setTagsAddNew(twinUpdateDTO.getTagsUpdate().newTags());
            }

            if (twinUpdateDTO.getFieldsAttributes() != null) {
                dst.setTwinFieldAttributeCUD(twinFieldAttributeCUDRestDTOReverseMapper.convert(twinUpdateDTO.getFieldsAttributes()));
            }
        }
    }
}
