package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsAssigneeForLinkedTwinContextField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerApiUserIsAssigneeForLinkedTwinContextFieldTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    @Mock
    private TwinLinkService twinLinkService;

    private ConditionerApiUserIsAssigneeForLinkedTwinContextField conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerApiUserIsAssigneeForLinkedTwinContextField();
        setField(conditioner, "authService", authService);
        setField(conditioner, "twinLinkService", twinLinkService);
        // lenient: the throwing tests below never reach the authService call
        lenient().when(authService.getApiUser()).thenReturn(apiUser);
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

    private Properties props(UUID fieldId) {
        var p = new Properties();
        p.put("twinClassFieldId", fieldId.toString());
        return p;
    }

    private FactoryItem itemWithContextField(UUID fieldId, FieldValue value) {
        var ctx = mock(FactoryContext.class);
        when(ctx.getFields()).thenReturn(Map.of(fieldId, value));
        // real FactoryItem so getOutput().getTwinId() (used in the size>1 error message) resolves
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_linkedTwinAssignerIsApiUser_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var apiUserId = UUID.randomUUID();
            when(apiUser.getUserId()).thenReturn(apiUserId);

            var link = mock(TwinLinkEntity.class);
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(1);
            when(fvl.getItems()).thenReturn(List.of(link));
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUserId()).thenReturn(apiUserId);
            when(twinLinkService.getDstTwinSafe(link)).thenReturn(dstTwin);

            assertTrue(conditioner.check(props(fieldId), itemWithContextField(fieldId, fvl)));
        }

        @Test
        void check_linkedTwinAssignerDiffersFromApiUser_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            when(apiUser.getUserId()).thenReturn(UUID.randomUUID());

            var link = mock(TwinLinkEntity.class);
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(1);
            when(fvl.getItems()).thenReturn(List.of(link));
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUserId()).thenReturn(UUID.randomUUID());
            when(twinLinkService.getDstTwinSafe(link)).thenReturn(dstTwin);

            assertFalse(conditioner.check(props(fieldId), itemWithContextField(fieldId, fvl)));
        }

        @Test
        void check_fieldValueIsNotLink_throws() throws ServiceException {
            // contract: only FieldValueLink is acceptable; otherwise FACTORY_PIPELINE_STEP_ERROR
            var fieldId = UUID.randomUUID();
            var notLink = mock(FieldValue.class); // not an instance of FieldValueLink

            assertThrows(ServiceException.class,
                    () -> conditioner.check(props(fieldId), itemWithContextField(fieldId, notLink)));
        }

        @Test
        void check_moreThanOneLinkedTwin_throws() throws ServiceException {
            // contract: a multi-link field is ambiguous; FACTORY_PIPELINE_STEP_ERROR
            var fieldId = UUID.randomUUID();
            var fvl = mock(FieldValueLink.class);
            when(fvl.size()).thenReturn(2);

            assertThrows(ServiceException.class,
                    () -> conditioner.check(props(fieldId), itemWithContextField(fieldId, fvl)));
        }
    }
}
