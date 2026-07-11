package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinOfClassAssignee;
import org.twins.core.service.factory.FactoryExecutionService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FillerBasicsAssigneeFromContextTwinOfClassAssigneeTest extends BaseUnitTest {

    @Mock
    private FactoryExecutionService twinFactoryService;

    private FillerBasicsAssigneeFromContextTwinOfClassAssignee filler;

    private static final UUID TWIN_CLASS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromContextTwinOfClassAssignee();
        inject(filler, "twinFactoryService", twinFactoryService);
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
        p.setProperty("twinClassId", TWIN_CLASS_ID.toString());
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
        void fill_foundTwinOfClass_setsItsAssignee() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var assigneeId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var contextTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assigneeId);
            when(twinFactoryService.lookupTwinOfClass(factoryItem, TWIN_CLASS_ID, 0)).thenReturn(contextTwin);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM the context twin of the given class.
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
        }

        @Test
        void fill_noTwinOfClassFound_throwsStepError() {
            var factoryItem = buildFactoryItem();
            when(twinFactoryService.lookupTwinOfClass(factoryItem, TWIN_CLASS_ID, 0)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_foundTwinWithNullAssignee_setsNull() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var contextTwin = new TwinEntity(); // assignee null
            when(twinFactoryService.lookupTwinOfClass(factoryItem, TWIN_CLASS_ID, 0)).thenReturn(contextTwin);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getAssignerUser());
            assertNull(outputTwin.getAssignerUserId());
        }
    }
}
