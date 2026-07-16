package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2364,
        name = "Backward link as context twin",
        description = "")
public class FillerBackwardLinkAsContextTwin extends FillerLinks {
    @FeaturerParam(name = "link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Uniq for src relink", description = "If true, then OneToOne and ManyToOne links will be relinked to new twin (if some other twin was already linked)", order = 2)
    public static final FeaturerParamBoolean uniqForSrcRelink = new FeaturerParamBoolean("uniqForSrcRelink");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin(); // all context twins must be the same class
        LinkEntity linkEntity = linkService.findEntitySafe(linkId.extract(properties));
        TwinOperation outputTwin = factoryItem.getOutput();
        addLink(outputTwin, new TwinLinkEntity()
                .setDstTwin(contextTwin) //setting dst, because TwinLinkService.prepareTwinLinks will hold it
                .setDstTwinId(contextTwin.getId())
                .setLink(linkEntity)
                .setLinkId(linkEntity.getId())
                .setUniqForSrcRelink(uniqForSrcRelink.extract(properties))
        );
    }
}
