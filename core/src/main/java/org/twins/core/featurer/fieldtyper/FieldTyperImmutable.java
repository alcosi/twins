package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Slf4j
public abstract class FieldTyperImmutable<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage, A extends TwinFieldValueSearch> extends FieldTyper<D, T, S, A>{
    @Override
    public void serializeValue(TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value != null && !twin.isCreateElseUpdate() && isUnchangedUpdate(twin, value)) {
            log.debug("{} is unchanged, serialization will be skipped", value.getTwinClassField().logNormal());
            return;
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "direct change of {} is not allowed", value.getTwinClassField().logNormal());
    }

    @Override
    public boolean isUnchangedUpdate(TwinEntity twin, T value) throws ServiceException {
        if (value.isUndefined() || value.isCleared())
            return false;
        if (!ensureFieldStorageLoaded(twin, value.getTwinClassField()))
            return false;
        Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams());
        T stored = deserializeValue(properties, new TwinField(twin, value.getTwinClassField()));
        return FieldValueContents.same(value, stored);
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    public boolean canSerialize(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return false;
    }
}
