package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dto.rest.twin.TwinStarredDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.StarredMode.class)
public class TwinStarredRestDTOMapper extends RestSimpleDTOMapper<TwinStarredEntity, TwinStarredDTOv1> {

    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @Override
    public void map(TwinStarredEntity src, TwinStarredDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.StarredMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setTwin(twinBaseRestDTOMapper.convert(src.getTwin(), mapperContext));
                break;
            case SHORT:
                dst
                        .setTwinId(src.getTwinId());
                break;
        }
    }
}
