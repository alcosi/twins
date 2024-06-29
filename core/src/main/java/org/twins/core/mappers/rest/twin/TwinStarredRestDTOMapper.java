package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStarredEntity;
import org.twins.core.dto.rest.twin.TwinStarredDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStarredRestDTOMapper extends RestSimpleDTOMapper<TwinStarredEntity, TwinStarredDTOv1> {
    final UserRestDTOMapper userRestDTOMapper;
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

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

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
