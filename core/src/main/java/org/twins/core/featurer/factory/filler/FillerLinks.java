package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinlink.TwinLinkCUD;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public abstract class FillerLinks extends Filler {
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Lazy
    @Autowired
    LinkService linkService;

    protected void addLinks(FactoryItem factoryItem, Collection<TwinLinkEntity> twinLinkList) throws ServiceException {
        twinLinkService.loadDstTwin(twinLinkList);
        twinLinkService.loadLink(twinLinkList);
        TwinOperation outputTwin = factoryItem.getOutput();
        for (TwinLinkEntity contextTwinLinkEntity : twinLinkList) {
            addLink(outputTwin, contextTwinLinkEntity.getLink(), contextTwinLinkEntity.getDstTwin());
        }
    }

    protected void addLink(TwinOperation twinOperation, LinkEntity link, TwinEntity toTwin) {
        addLink(twinOperation, link, toTwin, false);
    }

    protected void addLink(TwinOperation twinOperation, LinkEntity link, TwinEntity toTwin, boolean uniqForSrcRelink) {
        addLink(twinOperation, link, toTwin, LinkService.LinkDirection.forward, false);
    }

    protected void addLink(TwinOperation twinOperation, LinkEntity link, TwinEntity toTwin, LinkService.LinkDirection linkDirection, boolean uniqForSrcRelink) {
        var twinLinkCreate = new TwinLinkCreate()
                .setTwin(twinOperation.getTwinEntity())
                .setLink(link)
                .setLinkDirection(linkDirection)
                .addToTwin(toTwin)
                .setUniqForSrcRelink(uniqForSrcRelink);
        if (twinOperation instanceof TwinCreate twinCreate) {
            if (missed(twinCreate.getTwinEntity().getId(), twinCreate.getLinksEntityList(), twinLinkEntity))
                twinCreate.addLink(twinLinkCreate);
        } else if (twinOperation instanceof TwinUpdate twinUpdate) {
            if (twinUpdate.getTwinLinkCUD() == null)
                twinUpdate.setTwinLinkCUD(new TwinLinkCUD());
            if (missed(twinUpdate.getTwinEntity().getId(), twinUpdate.getTwinLinkCUD().getCreateEntityList(), twinLinkEntity))
                twinUpdate.getTwinLinkCUD().addCreate(twinLinkCreate);
        }
    }

    private boolean missed(UUID twinId, List<TwinLinkEntity> linksEntityList, TwinLinkEntity newLinkEntity) {
        if (CollectionUtils.isEmpty(linksEntityList))
            return true;
        for (TwinLinkEntity linkEntity : linksEntityList) {
            if (!linkEntity.getLinkId().equals(newLinkEntity.getLinkId()))
                continue;
            var id1ToCompare = newLinkEntity.getDstTwinId() != null && !newLinkEntity.getDstTwinId().equals(twinId) ? newLinkEntity.getDstTwinId() : newLinkEntity.getSrcTwinId();
            var id2ToCompare = linkEntity.getDstTwinId() != null && !linkEntity.getDstTwinId().equals(twinId) ? linkEntity.getDstTwinId() : linkEntity.getSrcTwinId();
            if (Objects.equals(id1ToCompare, id2ToCompare))
                return false;
        }
        return true;
    }
}
