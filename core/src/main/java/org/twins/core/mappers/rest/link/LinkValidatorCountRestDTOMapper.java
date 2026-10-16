package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.link.LinkValidatorCountDTOv1;
import org.twins.core.enums.sort.LinkValidatorGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.LinkValidatorMode;
import org.twins.core.service.link.LinkValidatorService;

import java.util.Collection;

@Component
@MapperModeBinding(modes = LinkValidatorMode.class)
@RequiredArgsConstructor
public class LinkValidatorCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<LinkValidatorEntity, LinkValidatorGroupField>, LinkValidatorCountDTOv1> {

    @MapperModePointerBinding(modes = LinkMode.LinkValidator2LinkMode.class)
    private final LinkRestDTOMapper linkRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.LinkValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final LinkValidatorService linkValidatorService;

    @Override
    public void map(CountResult<LinkValidatorEntity, LinkValidatorGroupField> src, LinkValidatorCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setCount(src.getCount());
        if (src.getGroupFields().contains(LinkValidatorGroupField.linkId))
            dst.setLinkId(entity.getLinkId());
        if (src.getGroupFields().contains(LinkValidatorGroupField.linkerFeaturerId))
            dst.setLinkerFeaturerId(entity.getLinkerFeaturerId());
        if (needLoad(mapperContext, LinkMode.LinkValidator2LinkMode.HIDE, src, LinkValidatorGroupField.linkId)) {
            linkValidatorService.loadLink(entity);
            linkRestDTOMapper.postpone(entity.getLink(), mapperContext.forkOnPoint(LinkMode.LinkValidator2LinkMode.SHORT));
        }
        if (needLoad(mapperContext, FeaturerMode.LinkValidator2FeaturerMode.HIDE, src, LinkValidatorGroupField.linkerFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getLinkerFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.LinkValidator2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<LinkValidatorEntity, LinkValidatorGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        if (entities.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, LinkMode.LinkValidator2LinkMode.HIDE, someCount, LinkValidatorGroupField.linkId)) {
            linkValidatorService.loadLink(entities);
        }
    }
}
