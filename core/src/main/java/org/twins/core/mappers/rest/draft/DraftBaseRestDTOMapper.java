package org.twins.core.mappers.rest.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.draft.DraftBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DraftMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DraftMode.class})
public class DraftBaseRestDTOMapper extends RestSimpleDTOMapper<DraftEntity, DraftBaseDTOv1> {
    @Override
    public void map(DraftEntity src, DraftBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DraftMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus())
                        .setCreatedByrUserId(src.getCreatedByUserId())
                        .setTwinCreateCount(useNullForZero(src.getTwinPersistCreateCount()))
                        .setTwinUpdateCount(useNullForZero(src.getTwinPersistUpdateCount()))
                        .setTwinDeleteCount(useNullForZero(src.getTwinEraseCount()))
                        .setTwinDeletedByStatusCount(useNullForZero(src.getTwinEraseByStatusCount()))
                        .setTwinDeletedIrrevocableCount(useNullForZero(src.getTwinEraseIrrevocableCount()))
                        .setTwinTagCreateCount(useNullForZero(src.getTwinTagCreateCount()))
                        .setTwinTagDeleteCount(useNullForZero(src.getTwinTagDeleteCount()))
                        .setTwinMarkerCreateCount(useNullForZero(src.getTwinMarkerCreateCount()))
                        .setTwinMarkerDeleteCount(useNullForZero(src.getTwinMarkerDeleteCount()))
                        .setTwinLinkCreateCount(useNullForZero(src.getTwinLinkCreateCount()))
                        .setTwinLinkDeleteCount(useNullForZero(src.getTwinLinkDeleteCount()))
                        .setTwinLinkUpdateCount(useNullForZero(src.getTwinLinkUpdateCount()))
                        .setTwinAttachmentCreateCount(useNullForZero(src.getTwinAttachmentCreateCount()))
                        .setTwinAttachmentDeleteCount(useNullForZero(src.getTwinAttachmentDeleteCount()))
                        .setTwinAttachmentUpdateCount(useNullForZero(src.getTwinAttachmentUpdateCount()))
                        .setTwinFieldSimpleCreateCount(useNullForZero(src.getTwinFieldSimpleCreateCount()))
                        .setTwinFieldSimpleUpdateCount(useNullForZero(src.getTwinFieldSimpleUpdateCount()))
                        .setTwinFieldSimpleDeleteCount(useNullForZero(src.getTwinFieldSimpleDeleteCount()))
                        .setTwinFieldUserCreateCount(useNullForZero(src.getTwinFieldUserCreateCount()))
                        .setTwinFieldUserUpdateCount(useNullForZero(src.getTwinFieldUserUpdateCount()))
                        .setTwinFieldUserDeleteCount(useNullForZero(src.getTwinFieldUserDeleteCount()))
                        .setTwinFieldDataListCreateCount(useNullForZero(src.getTwinFieldDataListCreateCount()))
                        .setTwinFieldDataListUpdateCount(useNullForZero(src.getTwinFieldDataListUpdateCount()))
                        .setTwinFieldDataListDeleteCount(useNullForZero(src.getTwinFieldDataListDeleteCount()))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus())
                        .setTwinCreateCount(useNullForZero(src.getTwinPersistCreateCount()))
                        .setTwinUpdateCount(useNullForZero(src.getTwinPersistUpdateCount()))
                        .setTwinDeleteCount(useNullForZero(src.getTwinEraseCount()));
                break;
        }
    }
    
    private static Integer useNullForZero(int value) {
        return value == 0 ? null : value;
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
