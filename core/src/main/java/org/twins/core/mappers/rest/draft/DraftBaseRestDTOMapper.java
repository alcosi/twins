package org.twins.core.mappers.rest.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.draft.DraftBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DraftMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinMode.class})
public class DraftBaseRestDTOMapper extends RestSimpleDTOMapper<DraftEntity, DraftBaseDTOv1> {
    @Override
    public void map(DraftEntity src, DraftBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DraftMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus())
                        .setCreatedByrUserId(src.getCreatedByUserId())
                        .setTwinCreateCount(src.getTwinCreateCount())
                        .setTwinUpdateCount(src.getTwinUpdateCount())
                        .setTwinDeleteCount(src.getTwinEraseCount())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(DraftMode.HIDE);
    }

    @Override
    public String getObjectCacheId(DraftEntity src) {
        return src.getId().toString();
    }

}
