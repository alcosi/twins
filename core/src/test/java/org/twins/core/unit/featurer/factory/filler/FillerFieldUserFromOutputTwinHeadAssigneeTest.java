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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldUserFromOutputTwinHeadAssignee;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldUserFromOutputTwinHeadAssigneeTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldUserFromOutputTwinHeadAssignee filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldUserFromOutputTwinHeadAssignee();
        inject(filler, "twinService", twinService);
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

    private FactoryItem buildFactoryItem(TwinEntity factoryItemTwin) {
        // getTwin() for a TwinCreate returns output.getTwinEntity(); set that twin so loadHead receives it.
        var output = new TwinCreate();
        output.setTwinEntity(factoryItemTwin);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_writesHeadTwinAssigneeToUserField() throws ServiceException {
            // NAME promises: write the OUTPUT TWIN's HEAD twin's ASSIGNEE into the user field.
            var factoryItemTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(factoryItemTwin);
            var assignee = new UserEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assignee.getId());
            when(twinService.loadHead(factoryItemTwin)).thenReturn(headTwin);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(new TwinClassFieldEntity().setId(FIELD_ID));

            filler.fill(props(), factoryItem, null);

            FieldValueUser stored = (FieldValueUser) factoryItem.getOutput().getField(FIELD_ID);
            assertEquals(1, stored.size());
            assertSame(assignee, stored.getItems().getFirst());
        }

        @Test
        void fill_noHeadTwin_throwsStepError() throws ServiceException {
            var factoryItemTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(factoryItemTwin);
            when(twinService.loadHead(factoryItemTwin)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_headWithoutAssignee_throwsStepError() throws ServiceException {
            var factoryItemTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(factoryItemTwin);
            var headTwin = new TwinEntity(); // assignerUserId == null
            when(twinService.loadHead(factoryItemTwin)).thenReturn(headTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
