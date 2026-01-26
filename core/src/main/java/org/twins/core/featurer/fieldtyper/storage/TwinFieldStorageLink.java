package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.link.TwinLinkService;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageLink extends TwinFieldStorage {
    private final TwinLinkService twinLinkService;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        twinLinkService.loadTwinLinks(twinsKit.getCollection());
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinLinks() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinLinks(TwinLinkService.FindTwinLinksResult.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
//        twinLinkService.deleteTwinLinks();
    };

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
