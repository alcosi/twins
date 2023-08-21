package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper implements RestDTOMapper<TwinEntity, TwinDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    public TwinDTOv1 convert(TwinEntity twinEntity) {
        TwinDTOv1 twinDTOv1 = new TwinDTOv1();
        map(twinEntity, twinDTOv1);
        return twinDTOv1;
    }

    @Override
    public void map(TwinEntity src, TwinDTOv1 dst) {
        dst
                .id(src.getId())
                .name(src.getName())
                .description(src.getDescription())
                .assignerUser(userDTOMapper.convert(src.getAssignerUser()))
                .authorUser(userDTOMapper.convert(src.getCreatedByUser()))
                .status(twinStatusRestDTOMapper.convert(src.getTwinStatus()))
                .createdAt(src.getCreatedAt().toInstant())
        ;
    }
}
