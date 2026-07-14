package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerFieldUserFromApiUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FillerFieldUserFromApiUserTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldUserFromApiUser filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldUserFromApiUser();
        inject(filler, "authService", authService);
        inject(filler, "twinClassFieldService", twinClassFieldService);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_writesApiUserIntoUserField() throws ServiceException {
            // NAME promises: write the API USER into the named user field on output.
            var fieldEntity = new TwinClassFieldEntity().setId(FIELD_ID);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity);
            var user = new UserEntity().setId(UUID.randomUUID());
            var apiUser = mock(ApiUser.class);
            when(apiUser.getUser()).thenReturn(user);
            when(authService.getApiUser()).thenReturn(apiUser);

            var factoryItem = buildFactoryItem();
            filler.fill(props(), List.of(factoryItem), null, false);

            FieldValueUser stored = (FieldValueUser) factoryItem.getOutput().getField(FIELD_ID);
            assertNotNull(stored);
            assertEquals(1, stored.size());
            assertSame(user, stored.getItems().getFirst());
        }
    }
}
