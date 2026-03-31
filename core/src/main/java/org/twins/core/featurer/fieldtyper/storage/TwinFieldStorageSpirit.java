package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSpirit extends TwinFieldStorage {
    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {

    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return true;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {

    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //nothing to delete
    }

}
