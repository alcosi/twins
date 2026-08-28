package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperSharedSelectInBusinessAccountTest extends BaseUnitTest {

    @Mock
    private DataListService dataListService;

    @Mock
    private DataListOptionService dataListOptionService;

    @Mock
    private AuthService authService;

    private FieldTyperSharedSelectInBusinessAccount fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperSharedSelectInBusinessAccount();
        setField(fieldTyper, "dataListService", dataListService);
        setField(fieldTyper, "dataListOptionService", dataListOptionService);
        setField(fieldTyper, "authService", authService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties properties(UUID listId) {
        var props = new Properties();
        props.setProperty("listUUID", listId.toString());
        return props;
    }

    @Nested
    class GetBusinessAccountId {

        @Test
        void getBusinessAccountId_noBusinessAccountSpecified_throws() throws ServiceException {
            // Intended: a shared-in-business-account select cannot resolve its scope without a business account.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(false);
            when(authService.getApiUser()).thenReturn(apiUser);

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.getBusinessAccountId(classField));

            assertEquals(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN.getCode(), ex.getErrorCode());
        }

        @Test
        void getBusinessAccountId_businessAccountSpecified_returnsId() throws ServiceException {
            // Intended: when the api user carries a business account, that id scopes the shared lookup.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var baId = UUID.randomUUID();
            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(baId);
            when(authService.getApiUser()).thenReturn(apiUser);

            assertEquals(baId, fieldTyper.getBusinessAccountId(classField));
        }
    }

    @Nested
    class CheckOptionAllowed {

        @Test
        void checkOptionAllowed_optionInUse_throws() throws ServiceException {
            // Intended: an option already consumed inside the business account must be rejected.
            var listId = UUID.randomUUID();
            var classFieldId = UUID.randomUUID();
            var baId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(classFieldId);
            var option = new DataListOptionEntity().setId(UUID.randomUUID()).setDataListId(listId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(baId);
            when(authService.getApiUser()).thenReturn(apiUser);
            // Option absent from the "not used" set → it IS in use.
            when(dataListService.findByDataListIdAndNotUsedInBusinessAccount(listId, classFieldId, baId))
                    .thenReturn(List.of());

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.checkOptionAllowed(twin, classField, option));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE.getCode(), ex.getErrorCode());
        }

        @Test
        void checkOptionAllowed_optionFree_returnsOptionId() throws ServiceException {
            // Intended: an option present in the free set is allowed and its id returned.
            var listId = UUID.randomUUID();
            var classFieldId = UUID.randomUUID();
            var baId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(classFieldId);
            var option = new DataListOptionEntity().setId(UUID.randomUUID()).setDataListId(listId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(baId);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(dataListService.findByDataListIdAndNotUsedInBusinessAccount(listId, classFieldId, baId))
                    .thenReturn(List.of(option));

            assertEquals(option.getId(), fieldTyper.checkOptionAllowed(twin, classField, option));
        }
    }

    @Nested
    class AllowMultiply {

        @Test
        void allowMultiply_alwaysFalse_sharedSelectIsSingleValued() throws ServiceException {
            // Intended: a shared select is single-valued regardless of params.
            assertFalse(fieldTyper.allowMultiply(properties(UUID.randomUUID())));
        }
    }
}
