package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

public abstract class FieldTyperCalcOnFly<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage, A extends TwinFieldValueSearch> extends FieldTyperImmutable<D, T, S, A> {

    @Autowired
    protected AuthService authService;

    protected PermissionContext calcPermissionContext() throws ServiceException {
        return new PermissionContext(
                authService.getApiUser().getUserId(),
                authService.getApiUser().getUser().getUserGroupsFootprint()
        );
    }
}
