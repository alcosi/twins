package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

@Slf4j
public abstract class FillerLinks extends Filler {
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Lazy
    @Autowired
    LinkService linkService;

    protected void addLinks(FactoryItem factoryItem, List<TwinLinkEntity> twinLinkList) {
        TwinOperation outputTwin = factoryItem.getOutputTwin();
        List<TwinLinkEntity> twinLinkEntityList = new ArrayList<>();
        for (TwinLinkEntity contextTwinLinkEntity : twinLinkList) {
            twinLinkEntityList.add(new TwinLinkEntity()
                    .setDstTwin(contextTwinLinkEntity.getDstTwin())
                    .setDstTwinId(contextTwinLinkEntity.getDstTwinId())
                    .setLink(contextTwinLinkEntity.getLink())
                    .setLinkId(contextTwinLinkEntity.getLinkId())
            );
        }
        addLinks(outputTwin, twinLinkEntityList);
    }

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
