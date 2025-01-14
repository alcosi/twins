package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2307,
        name = "ForwardLinksFromTemplateTwinAll",
        description = "")
public class FillerForwardLinksFromTemplateTwinAll extends FillerLinks {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (templateTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template twin");
        Kit<TwinLinkEntity, UUID> templateTwinLinkKit = twinLinkService.findTwinForwardLinks(templateTwin);
        if (KitUtils.isEmpty(templateTwinLinkKit))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No forward links configured from twmplate " + templateTwin.logShort());
        addLinks(factoryItem, templateTwinLinkKit.getCollection());
    }
}
