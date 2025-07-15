package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageAttachment implements TwinFieldStorage {
    private final TwinAttachmentRepository twinAttachmentRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit, Properties properties) {
        //nothing to load, because attachments should not be browsed as fields
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinAttachmentRepository.existByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return true;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        //nothing to init
    }
}
