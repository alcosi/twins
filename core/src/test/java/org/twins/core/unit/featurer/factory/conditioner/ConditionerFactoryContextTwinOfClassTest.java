package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryContextTwinOfClass;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryContextTwinOfClassTest extends BaseUnitTest {

    private ConditionerFactoryContextTwinOfClass conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryContextTwinOfClass();
    }

    private Properties props(UUID classId) {
        var p = new Properties();
        p.put("ofTwinClassId", classId.toString());
        return p;
    }

    private FactoryItem item(UUID twinClassId, FactoryItem contextSource) {
        var twin = new TwinEntity().setTwinClassId(twinClassId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        var item = new FactoryItem().setOutput(output);
        if (contextSource != null) {
            item.setContextFactoryItemList(List.of(contextSource));
        }
        return item;
    }

    private FactoryItem rootWith(FactoryItem... contextItems) {
        var ctx = mock(FactoryContext.class);
        when(ctx.getFactoryItemList()).thenReturn(new LinkedHashSet<>(Arrays.asList(contextItems)));
        return new FactoryItem().setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_contextItemMatchesClass_returnsTrue() throws ServiceException {
            // contract: shallow variant (depth=1) checks each factory-context item's OWN twin class.
            var targetClassId = UUID.randomUUID();
            var match = item(targetClassId, null);

            assertTrue(conditioner.check(props(targetClassId), rootWith(match)));
        }

        @Test
        void check_noContextItemMatchesClass_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var mismatch = item(UUID.randomUUID(), null);

            assertFalse(conditioner.check(props(targetClassId), rootWith(mismatch)));
        }

        @Test
        void check_emptyContextItemList_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();

            assertFalse(conditioner.check(props(targetClassId), rootWith()));
        }

        @Test
        void check_doesNotDescendIntoContextItemChildren_returnsFalse() throws ServiceException {
            // contract: depth capped at 1 — a class match only reachable by descending into a context
            // item's own source is NOT detected (the one descent hop dies at recursion counter 0).
            var targetClassId = UUID.randomUUID();
            var deeper = item(targetClassId, null); // would match if descended into
            var mid = item(UUID.randomUUID(), deeper); // own twin mismatches; its source is `deeper`

            assertFalse(conditioner.check(props(targetClassId), rootWith(mid)));
        }
    }
}
