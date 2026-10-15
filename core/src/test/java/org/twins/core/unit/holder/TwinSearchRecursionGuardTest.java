package org.twins.core.unit.holder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.TwinSearchRecursionGuard;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinSearchRecursionGuardTest {

    @AfterEach
    void cleanup() {
        TwinSearchRecursionGuard.clear();
    }

    @Test
    void sameSearchTwice_throwsAndNamesTheWholeChain() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertDoesNotThrow(() -> TwinSearchRecursionGuard.push(a));
        assertDoesNotThrow(() -> TwinSearchRecursionGuard.push(b));

        ServiceException e = assertThrows(ServiceException.class, () -> TwinSearchRecursionGuard.push(a));

        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), e.getErrorCode());
        // the message names the full cycle: a -> b -> a
        assertTrue(e.getMessage().contains("circular saved search: " + a + " -> " + b + " -> " + a),
                "message was: " + e.getMessage());
    }

    @Test
    void pop_allowsTheSearchToRunAgain() throws ServiceException {
        UUID a = UUID.randomUUID();
        TwinSearchRecursionGuard.push(a);
        TwinSearchRecursionGuard.pop(a);
        assertDoesNotThrow(() -> TwinSearchRecursionGuard.push(a)); // sequential re-run is legal
        TwinSearchRecursionGuard.pop(a);
    }

    @Test
    void nullId_isNoOp() {
        assertDoesNotThrow(() -> TwinSearchRecursionGuard.push(null));
        assertDoesNotThrow(() -> TwinSearchRecursionGuard.pop(null)); // must not throw NPE on the pair
    }
}
