package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinEntity src, TwinDTOv1 dst) throws Exception {
        dst
                .id(src.getId())
                .name(src.getName())
                .description(src.getDescription())
                .assignerUser(userDTOMapper.convert(src.getAssignerUser()))
                .authorUser(userDTOMapper.convert(src.getCreatedByUser()))
                .status(twinStatusRestDTOMapper.convert(src.getTwinStatus()))
                .twinClass(twinClassRestDTOMapper.convert(src.getTwinClass()))
                .createdAt(src.getCreatedAt().toInstant())
        ;
    }
}
