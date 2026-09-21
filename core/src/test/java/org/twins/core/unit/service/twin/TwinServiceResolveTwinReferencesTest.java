package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueReference;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.twin.TwinService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit contract of TwinService.resolveTwinReferences — the batch-aware seam behind materializeFieldValues
 * (REST field values, including relation twin fields) and featurer lookupers (output links):
 * ids of the not-yet-persisted twins of the current create batch (TemporalIdContext) resolve to the batch
 * entities themselves — the same instances the request will persist — with NO db call and no permission
 * check; everything else loads strictly (findEntitiesSafe: ifMissedThrows + read permission), so a
 * genuinely broken reference still fails fast.
 */
class TwinServiceResolveTwinReferencesTest extends BaseUnitTest {

    @Spy
    @InjectMocks
    private TwinService twinService;

    @Mock
    private TemporalIdContext temporalIdContext;

    @Captor
    private ArgumentCaptor<Collection<UUID>> idsCaptor;

    private UUID batchId;
    private UUID existingId;
    private TwinEntity batchTwin;
    private TwinEntity existingTwin;

    @BeforeEach
    void setUp() {
        batchId = UUID.randomUUID();
        existingId = UUID.randomUUID();
        batchTwin = new TwinEntity().setId(batchId);
        existingTwin = new TwinEntity().setId(existingId);
    }

    @Nested
    class ResolveTwinReferences {

        @Test
        void batchInternalId_resolvesToBatchEntityWithoutDbCall() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of(batchId, batchTwin));

            Map<UUID, TwinEntity> resolved = twinService.resolveTwinReferences(List.of(batchId));

            assertSame(batchTwin, resolved.get(batchId));
            verify(twinService, never()).findEntitiesSafe(any());
        }

        @Test
        void nonBatchId_loadsStrictly() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of());
            doReturn(new Kit<>(List.of(existingTwin), TwinEntity::getId)).when(twinService).findEntitiesSafe(any());

            Map<UUID, TwinEntity> resolved = twinService.resolveTwinReferences(List.of(existingId));

            assertSame(existingTwin, resolved.get(existingId));
            verify(twinService).findEntitiesSafe(idsCaptor.capture());
            assertEquals(Set.of(existingId), new HashSet<>(idsCaptor.getValue()));
        }

        @Test
        void mixedBatchAndExisting_strictLoadsOnlyForExistingIds() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of(batchId, batchTwin));
            doReturn(new Kit<>(List.of(existingTwin), TwinEntity::getId)).when(twinService).findEntitiesSafe(any());

            Map<UUID, TwinEntity> resolved = twinService.resolveTwinReferences(List.of(batchId, existingId));

            assertSame(batchTwin, resolved.get(batchId));       // batch entity, no db
            assertSame(existingTwin, resolved.get(existingId)); // strictly loaded
            verify(twinService).findEntitiesSafe(idsCaptor.capture());
            assertEquals(Set.of(existingId), new HashSet<>(idsCaptor.getValue())); // batch id never hits the db
        }

        @Test
        void nullId_isSkipped() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of());
            doReturn(new Kit<>(List.of(existingTwin), TwinEntity::getId)).when(twinService).findEntitiesSafe(any());
            List<UUID> withNull = new ArrayList<>(List.of(existingId));
            withNull.add(null);

            Map<UUID, TwinEntity> resolved = twinService.resolveTwinReferences(withNull);

            assertEquals(1, resolved.size());
        }

        @Test
        void missingNonBatchId_failsThroughStrictLoad() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of());
            doThrow(new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "twin not found")).when(twinService).findEntitiesSafe(any());

            assertThrows(ServiceException.class, () -> twinService.resolveTwinReferences(List.of(existingId)));
        }
    }

    @Nested
    class MaterializeFieldValues {

        @Test
        void linkReferenceToBatchInternalTwin_becomesFieldValueLinkWithBatchEntity_noDbCall() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of(batchId, batchTwin));
            FieldValueReference reference = new FieldValueReference(twinClassField(), FieldValueLink.class, List.of(batchId));

            List<FieldValue> out = twinService.materializeFieldValues(new ArrayList<>(List.of(reference)));

            assertInstanceOf(FieldValueLink.class, out.get(0));
            assertEquals(List.of(batchTwin), ((FieldValueLink) out.get(0)).getItems());
            verify(twinService, never()).findEntitiesSafe(any()); // pre-persist batch reference — the temporalId case
        }

        @Test
        void linkReferenceToExistingTwin_becomesFieldValueLinkWithLoadedEntity() throws ServiceException {
            when(temporalIdContext.getBatchTwinsById()).thenReturn(Map.of());
            doReturn(new Kit<>(List.of(existingTwin), TwinEntity::getId)).when(twinService).findEntitiesSafe(any());
            FieldValueReference reference = new FieldValueReference(twinClassField(), FieldValueLink.class, List.of(existingId));

            List<FieldValue> out = twinService.materializeFieldValues(new ArrayList<>(List.of(reference)));

            assertInstanceOf(FieldValueLink.class, out.get(0));
            assertEquals(List.of(existingTwin), ((FieldValueLink) out.get(0)).getItems());
        }
    }

    private TwinClassFieldEntity twinClassField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        return field;
    }
}
