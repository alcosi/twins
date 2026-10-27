package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.dto.rest.link.LinkValidatorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerParametrizedRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.LinkValidatorMode;
import org.twins.core.service.link.LinkValidatorService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {LinkValidatorMode.class})
public class LinkValidatorRestDTOMapper extends RestSimpleDTOMapper<LinkValidatorEntity, LinkValidatorDTOv1> {

    @MapperModePointerBinding(modes = LinkMode.LinkValidator2LinkMode.class)
    private final LinkRestDTOMapper linkRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.LinkValidator2FeaturerMode.class)
    private final FeaturerParametrizedRestDTOMapper featurerParametrizedRestDTOMapper;

    private final LinkValidatorService linkValidatorService;

    @Override
    public void map(LinkValidatorEntity src, LinkValidatorDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkValidatorMode.SHORT)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setLinkId(src.getLinkId())
                    .setOrder(src.getOrder())
                    .setLinkerFeaturerId(src.getLinkerFeaturerId())
                    .setLinkerParams(src.getLinkerParams());
            case SHORT -> dst
                    .setId(src.getId())
                    .setLinkId(src.getLinkId());
        }
        if (mapperContext.hasModeButNot(LinkMode.LinkValidator2LinkMode.HIDE)) {
            dst.setLinkId(src.getLinkId());
            linkValidatorService.loadLink(src);
            linkRestDTOMapper.postpone(src.getLink(), mapperContext.forkOnPoint(LinkMode.LinkValidator2LinkMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.LinkValidator2FeaturerMode.HIDE)) {
            dst.setLinkerFeaturerId(src.getLinkerFeaturerId());
            featurerParametrizedRestDTOMapper.postpone(src.getLinkerFeaturerId(), src.getLinkerParams(), mapperContext.forkOnPoint(FeaturerMode.LinkValidator2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<LinkValidatorEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(LinkMode.LinkValidator2LinkMode.HIDE))
            linkValidatorService.loadLink(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(LinkValidatorMode.HIDE);
    }
}
