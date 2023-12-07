package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public abstract class FillerLinks extends Filler {
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Lazy
    @Autowired
    LinkService linkService;

    protected void addLinks(TwinOperation outputTwin, List<TwinLinkEntity> twinLinkEntityList) {
        if (outputTwin instanceof TwinCreate twinCreate) {
            if (twinCreate.getLinksEntityList() == null)
                twinCreate.setLinksEntityList(twinLinkEntityList);
            else
                twinCreate.getLinksEntityList().addAll(twinLinkEntityList);
        } else if (outputTwin instanceof TwinUpdate twinUpdate) {
            if (twinUpdate.getTwinLinkCUD() == null)
                twinUpdate.setTwinLinkCUD(new EntityCUD<>());
            if (twinUpdate.getTwinLinkCUD().getCreateList() == null)
                twinUpdate.getTwinLinkCUD().setCreateList(twinLinkEntityList);
            else
                twinUpdate.getTwinLinkCUD().getCreateList().addAll(twinLinkEntityList);
        }
    }
}
