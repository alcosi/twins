package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageAttachment extends TwinFieldStorage {
    private final TwinAttachmentRepository twinAttachmentRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        //nothing to load, because attachments should not be browsed as fields
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinAttachmentRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return true;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        //nothing to init
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        twinAttachmentRepository.replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
