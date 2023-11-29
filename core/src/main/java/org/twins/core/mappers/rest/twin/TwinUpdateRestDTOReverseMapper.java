package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;


@Component
@RequiredArgsConstructor
public class TwinUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<Pair<TwinUpdateDTOv1, TwinEntity>, TwinUpdate> {
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final TwinAttachmentCUDRestDTOReverseMapper twinAttachmentCUDRestDTOReverseMapper;
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
                    .setAssignerUserId(userService.checkUserId(twinUpdateDTO.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                    .setDescription(twinUpdateDTO.getDescription());
            dst
                    .setUpdatedEntity(updatedTwinEntity)
                    .setUpdatedFields(twinFieldValueRestDTOReverseMapperV2.mapFields(dbTwinEntity.getTwinClassId(), twinUpdateDTO.getFields()))
                    .setAttachmentCUD(twinAttachmentCUDRestDTOReverseMapper.convert(twinUpdateDTO))
                    .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapper.convert(twinUpdateDTO));
        }
    }
}
