package org.twins.core.featurer.factory.filler;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.factory.FactoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2303,
        name = "FillerForwardLinksFromContextTwinAll",
        description = "")
public class FillerForwardLinksFromContextTwinAll extends FillerLinks {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = checkNotMultiplyContextTwin(factoryItem);
        if (contextTwin == null)
            return;
        List<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinForwardLinks(contextTwin.getId());
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            return;
        TwinOperation outputTwin = factoryItem.getOutputTwin();
        List<TwinLinkEntity> twinLinkEntityList = new ArrayList<>();
        for (TwinLinkEntity contextTwinLinkEntity : contextTwinLinksList) {
            twinLinkEntityList.add(new TwinLinkEntity()
                    .setDstTwin(contextTwinLinkEntity.getDstTwin())
                    .setDstTwinId(contextTwinLinkEntity.getDstTwinId())
                    .setLink(contextTwinLinkEntity.getLink())
                    .setLinkId(contextTwinLinkEntity.getLinkId())
            );
        }
        addLinks(outputTwin, twinLinkEntityList);
    }
}
