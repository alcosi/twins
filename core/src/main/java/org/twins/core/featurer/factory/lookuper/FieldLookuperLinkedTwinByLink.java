package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.link.TwinLinkService;

import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByLink extends FieldLookuper{
    @Autowired
    TwinLinkService twinLinkService;

    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException;
}
