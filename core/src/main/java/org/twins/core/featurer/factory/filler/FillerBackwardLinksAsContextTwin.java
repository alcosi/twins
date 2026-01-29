package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2308,
        name = "Backward links as context twin",
        description = "")
public class FillerBackwardLinksAsContextTwin extends FillerLinks {
    @FeaturerParam(name = "Uniq for src relink", description = "If true, then OneToOne and ManyToOne links will be relinked to new twin (if some other twin was already linked)", order = 1)
    public static final FeaturerParamBoolean uniqForSrcRelink = new FeaturerParamBoolean("uniqForSrcRelink");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin(); // all context twins must be the same class
        List<LinkEntity> linkEntityList = linkService.findLinks(contextTwin.getTwinClass(), factoryItem.getOutput().getTwinEntity().getTwinClass());
        if (CollectionUtils.isEmpty(linkEntityList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No links configured from " + contextTwin.getTwinClass().logShort() + " to " + factoryItem.getOutput().getTwinEntity().getTwinClass().logShort());
        if (linkEntityList.size() > 1) {
            log.warn(linkEntityList.size() + " links configured from " + contextTwin.getTwinClass().logShort() + " to " + factoryItem.getOutput().getTwinEntity().getTwinClass().logShort());
            //todo get link by hierarchy priority
        }
        LinkEntity linkEntity = linkEntityList.get(0);
        TwinOperation outputTwin = factoryItem.getOutput();
        List<TwinLinkEntity> twinLinkEntityList = new ArrayList<>();
        // todo why loop? we have already call factoryItem.checkSingleContextTwin();
        for (FactoryItem contextItem : factoryItem.getContextFactoryItemList()) {
            TwinEntity contextTwinEntity = contextItem.getTwin();
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
