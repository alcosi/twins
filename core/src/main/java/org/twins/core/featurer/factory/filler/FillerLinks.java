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
import java.util.Collection;
import java.util.List;

@Slf4j
public abstract class FillerLinks extends Filler {
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Lazy
    @Autowired
    LinkService linkService;

    protected void addLinks(FactoryItem factoryItem, Collection<TwinLinkEntity> twinLinkList) {
        TwinOperation outputTwin = factoryItem.getOutput();
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

    protected void addLink(TwinOperation outputTwin, TwinLinkEntity twinLinkEntity) {
        if (outputTwin instanceof TwinCreate twinCreate) {
            twinCreate.addLink(twinLinkEntity);
        } else if (outputTwin instanceof TwinUpdate twinUpdate) {
            if (twinUpdate.getTwinLinkCUD() == null)
                twinUpdate.setTwinLinkCUD(new EntityCUD<>());
            if (twinUpdate.getTwinLinkCUD().getCreateList() == null)
                twinUpdate.getTwinLinkCUD().setCreateList(new ArrayList<>());
            else
                twinUpdate.getTwinLinkCUD().getCreateList().add(twinLinkEntity);
        }
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
