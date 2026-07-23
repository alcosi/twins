package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.link.TwinLinkCountDTOv1;
import org.twins.core.enums.sort.TwinLinkGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinLinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinLinkMode.class)
public class TwinLinkCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinLinkEntity, TwinLinkGroupField>, TwinLinkCountDTOv1> {
    @MapperModePointerBinding(modes = TwinMode.TwinLink2TwinMode.class)
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinLink2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinLinkService twinLinkService;

    @Override
    public void map(CountResult<TwinLinkEntity, TwinLinkGroupField> src, TwinLinkCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setSrcTwinId(entity.getSrcTwinId())
                .setDstTwinId(entity.getDstTwinId())
                .setLinkId(entity.getLinkId())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinMode.TwinLink2TwinMode.HIDE, src, TwinLinkGroupField.srcTwinId)) {
            twinLinkService.loadSrcTwin(entity);
            twinBaseRestDTOMapper.postpone(entity.getSrcTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinLink2TwinMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinMode.TwinLink2TwinMode.HIDE, src, TwinLinkGroupField.dstTwinId)) {
            twinLinkService.loadDstTwin(entity);
            twinBaseRestDTOMapper.postpone(entity.getDstTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinLink2TwinMode.SHORT)));
        }
        if (needLoad(mapperContext, UserMode.TwinLink2UserMode.HIDE, src, TwinLinkGroupField.createdByUserId)) {
            twinLinkService.loadCreatedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinLink2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinLinkEntity, TwinLinkGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinMode.TwinLink2TwinMode.HIDE, someCount, TwinLinkGroupField.srcTwinId)) {
            twinLinkService.loadSrcTwin(entityCollection);
        }
        if (needLoad(mapperContext, TwinMode.TwinLink2TwinMode.HIDE, someCount, TwinLinkGroupField.dstTwinId)) {
            twinLinkService.loadDstTwin(entityCollection);
        }
        if (needLoad(mapperContext, UserMode.TwinLink2UserMode.HIDE, someCount, TwinLinkGroupField.createdByUserId)) {
            twinLinkService.loadCreatedByUser(entityCollection);
        }
    }
}
