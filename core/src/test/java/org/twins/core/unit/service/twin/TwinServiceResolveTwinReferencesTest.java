package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueReference;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.twin.TwinService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.twins.core.featurer.fieldtyper.FieldTyperList.LIST_SPLITTER;

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

    @Mock
    private FeaturerService featurerService;

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

    @Nested
    class ParseFieldValue {

        private TwinClassFieldEntity fieldTypedAs(Class<? extends FieldValue> valueType) throws ServiceException {
            var field = new TwinClassFieldEntity();
            field.setId(UUID.randomUUID());
            field.setFieldTyperFeaturerId(42);
            FieldTyper fieldTyper = mock(FieldTyper.class);
            when(featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class)).thenReturn(fieldTyper);
            when(fieldTyper.getValueType(field)).thenReturn((Class) valueType);
            return field;
        }

        @Test
        void linkTypedField_returnsReferenceCarrierWithoutLoading() throws ServiceException {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            var field = fieldTypedAs(FieldValueLink.class);

            FieldValue parsed = twinService.parseFieldValue(field, id1 + LIST_SPLITTER + id2);

            assertInstanceOf(FieldValueReference.class, parsed);
            var reference = (FieldValueReference) parsed;
            assertEquals(FieldValueLink.class, reference.getValueType());
            assertEquals(List.of(id1, id2), reference.getIds());
        }

        @Test
        void nullValue_parsesAsClearedConcreteValue() throws ServiceException {
            var field = fieldTypedAs(FieldValueUser.class);

            FieldValue parsed = twinService.parseFieldValue(field, null);

            assertInstanceOf(FieldValueUser.class, parsed);
            assertTrue(parsed.isCleared());
        }

        @Test
        void nullifyMarker_clearsTheWholeValue() throws ServiceException {
            var field = fieldTypedAs(FieldValueLink.class);

            FieldValue parsed = twinService.parseFieldValue(field, UuidUtils.NULLIFY_MARKER.toString());

            assertInstanceOf(FieldValueLink.class, parsed);
            assertTrue(parsed.isCleared());
        }

        @Test
        void incorrectUuid_failsFast() throws ServiceException {
            var field = fieldTypedAs(FieldValueLink.class);

            assertThrows(ServiceException.class, () -> twinService.parseFieldValue(field, "not-a-uuid"));
        }

        @Test
        void textTypedField_stillParsesInPlace() throws ServiceException {
            var field = fieldTypedAs(FieldValueText.class);

            FieldValue parsed = twinService.parseFieldValue(field, "hello");

            assertInstanceOf(FieldValueText.class, parsed);
            assertEquals("hello", ((FieldValueText) parsed).getValue());
        }
    }

    private TwinClassFieldEntity twinClassField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        return field;
    }
}
