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
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromOutputTwinHeadAssignee;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerBasicsAssigneeFromOutputTwinHeadAssigneeTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FillerBasicsAssigneeFromOutputTwinHeadAssignee filler;

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBasicsAssigneeFromOutputTwinHeadAssignee();
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

    private FactoryItem buildFactoryItem(TwinEntity factoryItemTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        // getTwin() returns output.getTwinEntity() for a TwinCreate; mirror that here.
        var factoryItem = new FactoryItem().setOutput(output);
        // To control factoryItem.getTwin() we rely on TwinCreate path: getTwin() == output.getTwinEntity().
        // Replace output's twin with the prepared twin so loadHead receives it.
        output.setTwinEntity(factoryItemTwin);
        return factoryItem;
    }

    @Nested
    class Fill {

        @Test
        void fill_loadsHeadAndSetsItsAssignee() throws ServiceException {
            var factoryItemTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(factoryItemTwin);

            var assigneeId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var headTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assigneeId);
            when(twinService.loadHead(factoryItemTwin)).thenReturn(headTwin);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM own head twin's assignee (lazily loaded).
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
            verify(twinService).loadHead(factoryItemTwin);
        }

        @Test
        void fill_noHeadTwin_throwsStepError() throws ServiceException {
            var factoryItemTwin = new TwinEntity();
            var factoryItem = buildFactoryItem(factoryItemTwin);
            when(twinService.loadHead(factoryItemTwin)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
