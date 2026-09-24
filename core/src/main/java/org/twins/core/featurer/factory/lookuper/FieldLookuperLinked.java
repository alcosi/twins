package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.factory.FactoryItemsBatch;

import java.util.UUID;

/**
 * Common batch contract of the linked-twin lookupers ({@link FieldLookuperLinkedTwinByField},
 * {@link FieldLookuperLinkedTwinByLink}) — lets batch callers hold either family in one type.
 */
public interface FieldLookuperLinked {

    LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedById, UUID lookupTwinClassFieldId) throws ServiceException;
}
