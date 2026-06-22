package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerTwinBasicFieldsFromContextBasics;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerTwinBasicFieldsFromContextBasicsTest extends BaseUnitTest {

    @Mock
    private UserService userService;

    private FillerTwinBasicFieldsFromContextBasics filler;

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerTwinBasicFieldsFromContextBasics();
        inject(filler, "userService", userService);
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

    private Properties props(TwinBasicFields.Basics... fields) {
        var p = new Properties();
        p.setProperty("fields",
                String.join(",",
                        java.util.Arrays.stream(fields).map(Enum::name).toList()));
        return p;
    }

    private FactoryItem buildFactoryItem(TwinBasicFields basics) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var ctx = new FactoryContext(null, null);
        ctx.setBasics(basics);
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    @Nested
    class Fill {

        @Test
        void fill_stringFields_copiedFromBasics() throws ServiceException {
            // NAME promises: copy the listed basic fields (name/description) from the factory context basics to the output twin.
            var basics = new TwinBasicFields();
            basics.setName("n");
            basics.setDescription("d");
            var factoryItem = buildFactoryItem(basics);

            filler.fill(props(TwinBasicFields.Basics.name, TwinBasicFields.Basics.description), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertEquals("n", outputTwin.getName());
            assertEquals("d", outputTwin.getDescription());
        }

        @Test
        void fill_userFields_usersResolvedAndAttached() throws ServiceException {
            var assigneeId = UUID.randomUUID();
            var creatorId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var creator = new UserEntity().setId(creatorId);
            var basics = new TwinBasicFields();
            basics.setAssigneeUserId(assigneeId);
            basics.setCreatedByUserId(creatorId);
            var factoryItem = buildFactoryItem(basics);
            when(userService.findEntitiesSafe(anyCollection()))
                    .thenReturn(new Kit<>(List.of(assignee, creator), UserEntity::getId));

            filler.fill(props(TwinBasicFields.Basics.assigneeUserId, TwinBasicFields.Basics.createdByUserId), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
            assertEquals(creatorId, outputTwin.getCreatedByUserId());
            assertSame(assignee, outputTwin.getAssignerUser());
            assertSame(creator, outputTwin.getCreatedByUser());
        }

        @Test
        void fill_nullBasics_isNoOp() throws ServiceException {
            // No basics in context -> nothing to copy, no throw.
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var ctx = new FactoryContext(null, null);
            var factoryItem = new FactoryItem().setOutput(output).setFactoryContext(ctx);

            filler.fill(props(TwinBasicFields.Basics.name), factoryItem, null);

            assertNull(factoryItem.getOutput().getTwinEntity().getName());
        }
    }
}
