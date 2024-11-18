package org.twins.core.featurer.factory.filler;

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

@Component
@Featurer(id = FeaturerTwins.ID_2304,
        name = "FillerBackwardLinksFromContextTwinAll",
        description = "")
public class FillerBackwardLinksFromContextTwinAll extends FillerLinks {
    @FeaturerParam(name = "uniqForSrcRelink", description = "If true, then XToOne links will be relinked to new twin")
    public static final FeaturerParamBoolean uniqForSrcRelink = new FeaturerParamBoolean("uniqForSrcRelink");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        List<TwinLinkEntity> contextTwinLinksList = twinLinkService.findTwinBackwardLinks(contextTwin.getId());
        if (CollectionUtils.isEmpty(contextTwinLinksList))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No backward links for contextTwin " + contextTwin.logShort());
        TwinOperation outputTwin = factoryItem.getOutput();
        List<TwinLinkEntity> twinLinkEntityList = new ArrayList<>();
        for (TwinLinkEntity contextTwinLinkEntity : contextTwinLinksList) {
            if (contextTwinLinkEntity.getLink().getType() == LinkEntity.TwinlinkType.ManyToMany
                    || uniqForSrcRelink.extract(properties))
                twinLinkEntityList.add(new TwinLinkEntity()
                        .setDstTwin(contextTwinLinkEntity.getSrcTwin()) //setting dst, because TwinLinkService.prepareTwinLinks will hold it
                        .setDstTwinId(contextTwinLinkEntity.getSrcTwinId())
                        .setLink(contextTwinLinkEntity.getLink())
                        .setLinkId(contextTwinLinkEntity.getLinkId())
                );
        }
        addLinks(outputTwin, twinLinkEntityList);
    }
}
