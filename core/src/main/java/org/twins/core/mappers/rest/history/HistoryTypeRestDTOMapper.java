package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dto.rest.history.HistoryTypeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryTypeMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = HistoryTypeMode.class)
public class HistoryTypeRestDTOMapper extends RestSimpleDTOMapper<HistoryTypeEntity, HistoryTypeDTOv1> {

    @Override
    public void map(HistoryTypeEntity src, HistoryTypeDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryTypeMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setSnapshotMessageTemplate(src.getSnapshotMessageTemplate());
            case SHORT:
                dst
                        .setId(src.getId());
        }
    }
}
