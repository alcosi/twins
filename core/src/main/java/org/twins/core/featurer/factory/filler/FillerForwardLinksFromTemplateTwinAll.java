package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2307,
        name = "FillerForwardLinksFromTemplateTwinAll",
        description = "")
public class FillerForwardLinksFromTemplateTwinAll extends FillerLinks {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (templateTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template twin");
        List<TwinLinkEntity> templateTwinLinkList = twinLinkService.findTwinForwardLinks(templateTwin.getId());
        if (CollectionUtils.isEmpty(templateTwinLinkList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No forward links configured from twmplate " + templateTwin.logShort());
        addLinks(factoryItem, templateTwinLinkList);
    }
}
