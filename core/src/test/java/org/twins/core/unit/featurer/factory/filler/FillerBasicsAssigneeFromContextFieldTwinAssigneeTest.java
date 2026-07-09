package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextFieldTwinAssignee;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerBasicsAssigneeFromContextFieldTwinAssigneeTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FillerBasicsAssigneeFromContextFieldTwinAssignee filler;

    private static final UUID LINK_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromContextFieldTwinAssignee();
        inject(filler, "twinService", twinService);
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
        p.setProperty("linkField", LINK_FIELD_ID.toString());
        return p;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private FactoryItem buildFactoryItem(Map<UUID, ?> fields) {
        var factoryContext = new FactoryContext(null, null).setFields(new HashMap<UUID, FieldValue>((Map) fields));
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem()
                .setOutput(output)
                .setFactoryContext(factoryContext);
    }

    private TwinClassFieldEntity buildField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("link");
        return field;
    }

    @Nested
    class Fill {

        @Test
        void fill_singleLink_setsAssigneeFromLinkedTwin() throws ServiceException {
            var dstTwinId = UUID.randomUUID();
            var link = new TwinLinkEntity().setDstTwinId(dstTwinId);
            var fieldValue = new FieldValueLink(buildField()).add(link);
            var factoryItem = buildFactoryItem(Map.of(LINK_FIELD_ID, fieldValue));

            var assignee = new UserEntity().setId(UUID.randomUUID());
            when(twinService.getTwinAssignee(dstTwinId)).thenReturn(assignee);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM the linked twin's assignee (looked up by dst twin id).
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assignee.getId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_missingContextField_throwsStepError() {
            var factoryItem = buildFactoryItem(new HashMap<>()); // no field for LINK_FIELD_ID

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_emptyLink_throwsStepError() throws ServiceException {
            var fieldValue = new FieldValueLink(buildField()); // undefined -> empty
            var factoryItem = buildFactoryItem(Map.of(LINK_FIELD_ID, fieldValue));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_linkedTwinHasNoAssignee_throwsStepError() throws ServiceException {
            var dstTwinId = UUID.randomUUID();
            var link = new TwinLinkEntity().setDstTwinId(dstTwinId);
            var fieldValue = new FieldValueLink(buildField()).add(link);
            var factoryItem = buildFactoryItem(Map.of(LINK_FIELD_ID, fieldValue));
            when(twinService.getTwinAssignee(dstTwinId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_nonLinkField_throwsStepError() throws ServiceException {
            var fieldValue = new FieldValueText(buildField());
            var factoryItem = buildFactoryItem(Map.of(LINK_FIELD_ID, fieldValue));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }
}
