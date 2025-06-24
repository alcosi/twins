package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.link.TwinLinkService;

import java.util.UUID;

public abstract class FieldLookuperLinkedTwinByField extends FieldLookuper {
    @Autowired
    protected TwinLinkService twinLinkService;

    public abstract FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException;
}
