package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopy;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedCopyTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    private MultiplierIsolatedCopy multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedCopy();
        setField(multiplier, "authService", authService);
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

    private Properties buildProperties(boolean copyHead) {
        var props = new Properties();
        props.put("copyHead", Boolean.toString(copyHead));
        return props;
    }

    private FactoryItem buildInputItem(UUID twinClassId, UUID headTwinId) {
        var twinClass = new TwinClassEntity();
        twinClass.setId(twinClassId);
        var twin = new TwinEntity()
                .setId(UUID.randomUUID())
                .setTwinClass(twinClass)
                .setTwinClassId(twinClassId);
        if (headTwinId != null) {
            var head = new TwinEntity().setId(headTwinId);
            twin.setHeadTwin(head).setHeadTwinId(headTwinId);
        }
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    private ApiUser stubApiUser() throws ServiceException {
        var apiUser = mock(ApiUser.class);
        var user = new UserEntity().setId(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(user);
        return apiUser;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_multiInput_producesOneCopyPerInputWithInputClass() throws ServiceException {
            var classId = UUID.randomUUID();
            var props = buildProperties(false);
            var input = List.of(
                    buildInputItem(classId, null),
                    buildInputItem(classId, null),
                    buildInputItem(classId, null));

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, input, mock(FactoryContext.class));

            // contract: "New output twin for each input" -> 3 in, 3 out
            assertEquals(3, result.size());

            for (int i = 0; i < result.size(); i++) {
                var out = (TwinCreate) result.get(i).getOutput();
                // output class is taken from the input twin
                assertEquals(classId, out.getTwinEntity().getTwinClassId());
                // each output is wired only to its own source input (isolated scope)
                assertEquals(1, result.get(i).getContextFactoryItemList().size());
                assertSame(input.get(i), result.get(i).getContextFactoryItemList().get(0));
                // copyHead=false -> new twin must NOT inherit the head
                assertNull(out.getTwinEntity().getHeadTwinId());
            }
        }

        @Test
        void multiply_copyHeadTrue_newTwinInheritsHeadFromInput() throws ServiceException {
            var classId = UUID.randomUUID();
            var headId = UUID.randomUUID();
            var props = buildProperties(true);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem(classId, headId)), mock(FactoryContext.class));

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertEquals(headId, out.getTwinEntity().getHeadTwinId());
            assertNotNull(out.getTwinEntity().getHeadTwin());
            assertEquals(headId, out.getTwinEntity().getHeadTwin().getId());
        }

        @Test
        void multiply_copyHeadFalse_newTwinHasNoHeadEvenIfInputHasOne() throws ServiceException {
            var classId = UUID.randomUUID();
            var headId = UUID.randomUUID();
            var props = buildProperties(false);

            var apiUser = stubApiUser();

            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem(classId, headId)), mock(FactoryContext.class));

            assertEquals(1, result.size());
            var out = (TwinCreate) result.get(0).getOutput();
            assertNull(out.getTwinEntity().getHeadTwinId());
            assertNull(out.getTwinEntity().getHeadTwin());
        }

        @Test
        void multiply_createdByUserPropagatedFromApiUser() throws ServiceException {
            var classId = UUID.randomUUID();
            var props = buildProperties(false);

            var apiUser = mock(ApiUser.class);
            var user = new UserEntity().setId(UUID.randomUUID());
            when(apiUser.getUser()).thenReturn(user);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = multiplier.multiply(props, List.of(buildInputItem(classId, null)), mock(FactoryContext.class));

            var out = (TwinCreate) result.get(0).getOutput();
            assertSame(user, out.getTwinEntity().getCreatedByUser());
            assertEquals(user.getId(), out.getTwinEntity().getCreatedByUserId());
            assertNotNull(out.getTwinEntity().getCreatedAt());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var result = multiplier.multiply(buildProperties(false), List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }
    }
}
