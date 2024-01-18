package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinBaseRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv1> {
    @Override
    public void map(TwinEntity src, TwinBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinMode.SHORT)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .externalId(src.getExternalId())
                        .headTwinId(src.getHeadTwinId())
                        .assignerUserId(src.getAssignerUserId())
                        .authorUserId(src.getCreatedByUserId())
                        .statusId(src.getTwinStatusId())
                        .twinClassId(src.getTwinClassId())
                        .description(src.getDescription())
                        .createdAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum TwinMode implements MapperMode {
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
