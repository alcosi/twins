package org.twins.core.mappers.rest.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.draft.DraftDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DraftMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinMode.class})
public class DraftRestDTOMapper extends RestSimpleDTOMapper<DraftEntity, DraftDTOv1> {
    final DraftBaseRestDTOMapper draftBaseRestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(DraftEntity src, DraftDTOv1 dst, MapperContext mapperContext) throws Exception {
        draftBaseRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.Draft2UserMode.HIDE)) {
            dst.setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT))));
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
