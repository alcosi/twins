package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDependent;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TwinFieldStorageDependentTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private FieldTyper<?, ?, ?, ?> operandTyper;
    @Mock
    private TwinFieldStorage operandStorage;

    private UUID sumFieldId;
    private UUID operandId;
    private TwinClassFieldEntity operandTcf;
    private TwinFieldStorageDependent storage;

    @BeforeEach
    void setUp() throws ServiceException {
        sumFieldId = UUID.randomUUID();
        operandId = UUID.randomUUID();
        operandTcf = new TwinClassFieldEntity().setId(operandId).setFieldTyperFeaturerId(42);
        storage = new TwinFieldStorageDependent(
                sumFieldId, Set.of(operandId), featurerService, twinClassFieldService);
    }

    private Kit<TwinEntity, UUID> kitOf(TwinEntity... twins) {
        return new Kit<>(List.of(twins), TwinEntity::getId);
    }

    private void stubOperandChain(TwinEntity twin, boolean operandAlreadyLoaded) throws ServiceException {
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(operandTcf), TwinClassFieldEntity::getId));
        when(featurerService.getFeaturer(eq(42), any())).thenReturn(operandTyper);
        when(operandTyper.getStorage(operandTcf)).thenReturn(operandStorage);
        when(operandStorage.isLoaded(twin)).thenReturn(operandAlreadyLoaded);
    }

    @Nested
    class IsLoaded {
        @Test
        void isLoaded_falseBeforeLoad() {
            var twin = new TwinEntity();
            assertFalse(storage.isLoaded(twin));
        }

        @Test
        void isLoaded_trueAfterLoad_andDoesNotWriteFieldValue() throws ServiceException {
            var twin = new TwinEntity().setId(UUID.randomUUID());
            stubOperandChain(twin, true);

            storage.load(kitOf(twin));

            assertTrue(storage.isLoaded(twin));
            // Storage must not write the Sum's own value — that is the FieldTyper's job.
            assertNull(twin.getTwinFieldCalculated());
        }
    }

    @Nested
    class Load {
        @Test
        void load_invokesFindEntitiesSafeBulkForOperands() throws ServiceException {
            var twin = new TwinEntity().setId(UUID.randomUUID());
            stubOperandChain(twin, true);

            storage.load(kitOf(twin));

            verify(twinClassFieldService).findEntitiesSafe(Set.of(operandId));
        }

        @Test
        void load_loadsOperandStorageForUnloadedTwins() throws ServiceException {
            var twin = new TwinEntity().setId(UUID.randomUUID());
            stubOperandChain(twin, false); // operand not loaded yet

            storage.load(kitOf(twin));

            verify(operandStorage).load(any());
        }

        @Test
        void load_skipsAlreadyLoadedOperandStorage() throws ServiceException {
            var twin = new TwinEntity().setId(UUID.randomUUID());
            stubOperandChain(twin, true); // operand already loaded

            storage.load(kitOf(twin));

            verify(operandStorage, never()).load(any());
        }

        @Test
        void load_emptyOperandsStillMarksLoaded() throws ServiceException {
            var emptyStorage = new TwinFieldStorageDependent(
                    sumFieldId, Set.of(), featurerService, twinClassFieldService);
            var twin = new TwinEntity().setId(UUID.randomUUID());

            emptyStorage.load(kitOf(twin));

            assertTrue(emptyStorage.isLoaded(twin));
            verifyNoInteractions(featurerService);
        }
    }

    @Nested
    class CanBeMerged {
        @Test
        void equals_trueForSameFieldAndOperands() {
            var other = new TwinFieldStorageDependent(
                    sumFieldId, Set.of(operandId), featurerService, twinClassFieldService);
            assertEquals(storage, other);
        }

        @Test
        void equals_falseForDifferentField() {
            var other = new TwinFieldStorageDependent(
                    UUID.randomUUID(), Set.of(operandId), featurerService, twinClassFieldService);
            assertNotEquals(storage, other);
        }

        @Test
        void equals_falseForDifferentOperands() {
            var other = new TwinFieldStorageDependent(
                    sumFieldId, Set.of(UUID.randomUUID()), featurerService, twinClassFieldService);
            assertNotEquals(storage, other);
        }
    }
}
