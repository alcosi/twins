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
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryContextTwinOfClassDeep;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryContextTwinOfClassDeepTest extends BaseUnitTest {

    private ConditionerFactoryContextTwinOfClassDeep conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryContextTwinOfClassDeep();
    }

    private Properties props(UUID classId) {
        var p = new Properties();
        p.put("ofTwinClassId", classId.toString());
        return p;
    }

    // Real FactoryItem: getTwin() reads the output twin, checkNotMultiplyContextItem() reads the
    // context list. Both satisfied without stubbing, so no UnnecessaryStubbing regardless of which
    // branch the check takes.
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

    // FactoryContext.getFactoryItemList() filters by factoryBranchId/pipeline, so the context itself
    // stays mocked — but only its getFactoryItemList(); the items inside are real.
    private FactoryItem rootWithContextItems(Set<FactoryItem> contextItems) {
        var ctx = mock(FactoryContext.class);
        when(ctx.getFactoryItemList()).thenReturn(contextItems);
        return new FactoryItem().setFactoryContext(ctx);
    }

    @Nested
    class Check {

        @Test
        void check_immediateContextItemMatches_returnsTrue() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var match = item(targetClassId, null);

            assertTrue(conditioner.check(props(targetClassId),
                    rootWithContextItems(new LinkedHashSet<>(Set.of(match)))));
        }

        @Test
        void check_emptyContextList_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var root = rootWithContextItems(new LinkedHashSet<>());

            assertFalse(conditioner.check(props(targetClassId), root));
        }

        @Test
        void check_matchDeeperInChain_returnsTrue() throws ServiceException {
            // contract: Deep variant walks checkNotMultiplyContextItem() down the chain.
            // chain: top (mismatch) -> mid (mismatch) -> match
            var targetClassId = UUID.randomUUID();
            var match = item(targetClassId, null);
            var mid = item(UUID.randomUUID(), match);
            var top = item(UUID.randomUUID(), mid);

            assertTrue(conditioner.check(props(targetClassId),
                    rootWithContextItems(new LinkedHashSet<>(Set.of(top)))));
        }

        @Test
        void check_chainExhaustedWithoutMatch_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var bottom = item(UUID.randomUUID(), null);
            var top = item(UUID.randomUUID(), bottom);

            assertFalse(conditioner.check(props(targetClassId),
                    rootWithContextItems(new LinkedHashSet<>(Set.of(top)))));
        }

        @Test
        void check_recursionBoundedToFive_returnsFalse() throws ServiceException {
            // contract: recursionCounter starts at 5 — five context levels are inspected.
            // Six mismatching levels so the 6th is never inspected within the cap.
            var targetClassId = UUID.randomUUID();
            var l6 = item(UUID.randomUUID(), null);
            var l5 = item(UUID.randomUUID(), l6);
            var l4 = item(UUID.randomUUID(), l5);
            var l3 = item(UUID.randomUUID(), l4);
            var l2 = item(UUID.randomUUID(), l3);
            var l1 = item(UUID.randomUUID(), l2);

            assertFalse(conditioner.check(props(targetClassId),
                    rootWithContextItems(new LinkedHashSet<>(Set.of(l1)))));
        }

        @Test
        void check_matchAtFifthLevel_returnsTrue() throws ServiceException {
            // contract: the fifth inspected context level is still within the cap.
            var targetClassId = UUID.randomUUID();
            var l5 = item(targetClassId, null);
            var l4 = item(UUID.randomUUID(), l5);
            var l3 = item(UUID.randomUUID(), l4);
            var l2 = item(UUID.randomUUID(), l3);
            var l1 = item(UUID.randomUUID(), l2);

            assertTrue(conditioner.check(props(targetClassId),
                    rootWithContextItems(new LinkedHashSet<>(Set.of(l1)))));
        }
    }
}
