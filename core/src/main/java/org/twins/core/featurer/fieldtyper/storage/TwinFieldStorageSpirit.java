package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageSpirit implements TwinFieldStorage {
    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit, Properties properties) {

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
}
