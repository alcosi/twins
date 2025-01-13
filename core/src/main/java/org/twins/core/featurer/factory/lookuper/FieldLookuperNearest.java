package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

public abstract class FieldLookuperNearest extends FieldLookuper {
    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException;
}
