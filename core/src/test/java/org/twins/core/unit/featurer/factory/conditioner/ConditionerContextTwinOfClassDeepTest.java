package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinOfClassDeep;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerContextTwinOfClassDeepTest extends BaseUnitTest {

    private ConditionerContextTwinOfClassDeep conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerContextTwinOfClassDeep();
    }

    private Properties buildProperties(UUID classId) {
        var props = new Properties();
        props.put("ofTwinClassId", classId.toString());
        return props;
    }

    // A real FactoryItem whose twin carries twinClassId and whose single context source is `contextSource`
    // (null = leaf, no further context). checkNotMultiplyContextItem() reads contextFactoryItemList,
    // and getTwin() reads the output twin entity — both satisfied by real objects with no stubbing.
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

    @Nested
    class Check {

        @Test
        void check_immediateContextMatches_returnsTrue() throws ServiceException {
            // contract: the deep variant checks the factoryItem's CONTEXT chain (via
            // checkNotMultiplyContextItem), not the factoryItem's own twin. Match at depth 1 -> true.
            var targetClassId = UUID.randomUUID();
            var match = item(targetClassId, null);
            var root = item(UUID.randomUUID(), match);

            assertTrue(conditioner.check(buildProperties(targetClassId), root));
        }

        @Test
        void check_noContextItem_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var root = item(UUID.randomUUID(), null);

            assertFalse(conditioner.check(buildProperties(targetClassId), root));
        }

        @Test
        void check_matchesDeeperInChain_returnsTrue() throws ServiceException {
            // contract: deep variant walks the context chain until a matching twin class is found.
            // chain: root -> mid (mismatch) -> match
            var targetClassId = UUID.randomUUID();
            var match = item(targetClassId, null);
            var mid = item(UUID.randomUUID(), match);
            var root = item(UUID.randomUUID(), mid);

            assertTrue(conditioner.check(buildProperties(targetClassId), root));
        }

        @Test
        void check_chainExhaustedWithoutMatch_returnsFalse() throws ServiceException {
            // contract: when no level matches and the chain ends (null context), result is false
            var targetClassId = UUID.randomUUID();
            var bottom = item(UUID.randomUUID(), null);
            var root = item(UUID.randomUUID(), bottom);

            assertFalse(conditioner.check(buildProperties(targetClassId), root));
        }

        @Test
        void check_recursionBoundedToFive_returnsFalse() throws ServiceException {
            // contract: recursion is capped at depth 5 ("hope 5 is more than enough").
            // Six mismatching levels so no match is ever found within the cap; the 6th is never inspected.
            var targetClassId = UUID.randomUUID();
            var l6 = item(UUID.randomUUID(), null);
            var l5 = item(UUID.randomUUID(), l6);
            var l4 = item(UUID.randomUUID(), l5);
            var l3 = item(UUID.randomUUID(), l4);
            var l2 = item(UUID.randomUUID(), l3);
            var l1 = item(UUID.randomUUID(), l2);
            var root = item(UUID.randomUUID(), l1);

            assertFalse(conditioner.check(buildProperties(targetClassId), root));
        }

        @Test
        void check_matchAtFifthLevel_returnsTrue() throws ServiceException {
            // contract: the fifth inspected context level is still within the recursion cap.
            var targetClassId = UUID.randomUUID();
            var l5 = item(targetClassId, null);
            var l4 = item(UUID.randomUUID(), l5);
            var l3 = item(UUID.randomUUID(), l4);
            var l2 = item(UUID.randomUUID(), l3);
            var l1 = item(UUID.randomUUID(), l2);
            var root = item(UUID.randomUUID(), l1);

            assertTrue(conditioner.check(buildProperties(targetClassId), root));
        }
    }
}
