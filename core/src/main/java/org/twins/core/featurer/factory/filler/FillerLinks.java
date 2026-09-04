package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinlink.TwinLinkCUD;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.*;

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
            if (missed(twinCreate.getTwinEntity().getId(), twinCreate.getLinksEntityList(), twinLinkEntity))
                twinCreate.addLink(twinLinkEntity);
        } else if (outputTwin instanceof TwinUpdate twinUpdate) {
            if (twinUpdate.getTwinLinkCUD() == null)
                twinUpdate.setTwinLinkCUD(new TwinLinkCUD());
            if (missed(twinUpdate.getTwinEntity().getId(), twinUpdate.getTwinLinkCUD().getCreateEntityList(), twinLinkEntity))
                twinUpdate.getTwinLinkCUD().addCreate(twinLinkEntity);
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

    protected void addLinks(TwinOperation outputTwin, List<TwinLinkEntity> twinLinkEntityList) {
        for (TwinLinkEntity twinLinkEntity : twinLinkEntityList) {
            addLink(outputTwin, twinLinkEntity);
        }
    }
}
