package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.link.LinkCountDTOv1;
import org.twins.core.enums.sort.LinkGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.link.LinkService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = LinkMode.class)
public class LinkCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<LinkEntity, LinkGroupField>, LinkCountDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.LinkSrc2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final LinkService linkService;

    @Override
    public void map(CountResult<LinkEntity, LinkGroupField> src, LinkCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setSrcTwinClassId(entity.getSrcTwinClassId())
                .setDstTwinClassId(entity.getDstTwinClassId())
                .setType(entity.getType())
                .setLinkStrength(entity.getLinkStrengthId())
                .setSrcTwinClassInheritable(entity.getSrcTwinClassInheritable())
                .setDstTwinClassInheritable(entity.getDstTwinClassInheritable())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinClassMode.LinkSrc2TwinClassMode.HIDE, src, LinkGroupField.srcTwinClassId)) {
            linkService.loadSrcTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getSrcTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.LinkSrc2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.LinkDst2TwinClassMode.HIDE, src, LinkGroupField.dstTwinClassId)) {
            linkService.loadDstTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getDstTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.LinkDst2TwinClassMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<LinkEntity, LinkGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.LinkSrc2TwinClassMode.HIDE, someCount, LinkGroupField.srcTwinClassId)) {
            linkService.loadSrcTwinClass(entityCollection);
        }
        if (needLoad(mapperContext, TwinClassMode.LinkDst2TwinClassMode.HIDE, someCount, LinkGroupField.dstTwinClassId)) {
            linkService.loadDstTwinClass(entityCollection);
        }
    }
}
