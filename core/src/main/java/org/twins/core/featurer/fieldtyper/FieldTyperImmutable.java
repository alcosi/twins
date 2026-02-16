package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

public abstract class FieldTyperImmutable<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage, A extends TwinFieldSearch> extends FieldTyper<D, T, S, A>{
    @Override
    public void serializeValue(TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "direct change of {} is not allowed", value.getTwinClassField().logNormal());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    public boolean canSerialize(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return false;
    }
}
