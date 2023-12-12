package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = 2308,
        name = "FillerBackwardLinksAsContextTwin",
        description = "")
public class FillerBackwardLinksAsContextTwin extends FillerLinks {
    @FeaturerParam(name = "uniqForSrcRelink", description = "If true, then OneToOne and ManyToOne links will be relinked to new twin (if some other twin was already linked)")
    public static final FeaturerParamBoolean uniqForSrcRelink = new FeaturerParamBoolean("uniqForSrcRelink");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.getContextFirstTwin(); // all context twins must be the same class
        if (contextTwin == null)
            return;
        List<LinkEntity> linkEntityList = linkService.findLinks(contextTwin.getTwinClass(), factoryItem.getOutputTwin().getTwinEntity().getTwinClass());
        if (CollectionUtils.isEmpty(linkEntityList)) {
            log.warn("No links configured from " + contextTwin.getTwinClass().logShort() + " to " + factoryItem.getOutputTwin().getTwinEntity().getTwinClass().logShort());
            return;
        }
        if (linkEntityList.size() > 1) {
            log.warn(linkEntityList.size() + " links configured from " + contextTwin.getTwinClass().logShort() + " to " + factoryItem.getOutputTwin().getTwinEntity().getTwinClass().logShort());
            //todo get link by hierarchy priority
        }
        LinkEntity linkEntity = linkEntityList.get(0);
        TwinOperation outputTwin = factoryItem.getOutputTwin();
        List<TwinLinkEntity> twinLinkEntityList = new ArrayList<>();
        for (TwinEntity contextTwinEntity : factoryItem.getContextTwinList()) {
                twinLinkEntityList.add(new TwinLinkEntity()
                        .setDstTwin(contextTwinEntity) //setting dst, because TwinLinkService.prepareTwinLinks will hold it
                        .setDstTwinId(contextTwinEntity.getId())
                        .setLink(linkEntity)
                        .setLinkId(linkEntity.getId())
                        .setUniqForSrcRelink(uniqForSrcRelink.extract(properties))
                );
        }
        addLinks(outputTwin, twinLinkEntityList);
    }
}
