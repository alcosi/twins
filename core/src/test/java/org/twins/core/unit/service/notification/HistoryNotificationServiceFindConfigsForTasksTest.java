package org.twins.core.unit.service.notification;

import org.cambium.common.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryTypeService;
import org.twins.core.service.notification.*;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinvalidator.TwinValidatorSetService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Bulk config matcher of {@link HistoryNotificationService#findConfigsForTasks}: one candidate query
 * over the union of (historyType, twinClassId incl. extended chain, schemaId), in-memory matching
 * with the original twinClassFieldId null-semantics (task fieldId == null → any config; else exact)
 * and the per-config validator filter filling both chunk projections.
 */
class HistoryNotificationServiceFindConfigsForTasksTest extends BaseUnitTest {

    @Mock
    private HistoryNotificationRepository repository;
    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private TwinClassService twinClassService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private TwinValidatorSetService twinValidatorSetService;
    @Mock
    private NotificationSchemaService notificationSchemaService;
    @Mock
    private NotificationEventServiceService notificationEventServiceService;
    @Mock
    private HistoryNotificationRecipientService historyNotificationRecipientService;
    @Mock
    private HistoryTypeService historyTypeService;

    private HistoryNotificationService service;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        service = new HistoryNotificationService(
                repository, authService, userService, twinClassService, twinClassFieldService,
                twinValidatorSetService, notificationSchemaService, notificationEventServiceService,
                historyNotificationRecipientService, historyTypeService);
        domainId = UUID.randomUUID();
    }

    // ---------- test data builders ----------

    private TwinClassEntity twinClass(UUID id, UUID... extended) {
        var twinClass = new TwinClassEntity();
        twinClass.setId(id);
        twinClass.setDomainId(domainId);
        if (extended.length > 0) {
            twinClass.setExtendedClassIdSet(new HashSet<>(List.of(extended)));
        }
        return twinClass;
    }

    private HistoryNotificationTaskEntity task(HistoryType historyType, UUID schemaId, TwinClassEntity twinClass, UUID fieldId) {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setTwinClassId(twinClass.getId()); // raw FK — the matcher keys on it, not on the relation object
        twin.setTwinClass(twinClass);
        var history = new HistoryEntity();
        history.setHistoryType(historyType);
        history.setTwinId(twin.getId());
        history.setTwin(twin);
        history.setTwinClassFieldId(fieldId);
        return new HistoryNotificationTaskEntity()
                .setId(UUID.randomUUID())
                .setNotificationSchemaId(schemaId)
                .setHistory(history);
    }

    private HistoryNotificationEntity config(HistoryType historyType, UUID schemaId, UUID twinClassId, UUID fieldId) {
        var config = new HistoryNotificationEntity();
        config.setId(UUID.randomUUID());
        config.setHistoryTypeId(historyType);
        config.setNotificationSchemaId(schemaId);
        config.setTwinClassId(twinClassId);
        config.setTwinClassFieldId(fieldId);
        return config;
    }

    private HistoryNotificationChunk chunkOf(HistoryNotificationTaskEntity... tasks) {
        return new HistoryNotificationChunk(domainId, List.of(tasks));
    }

    private void candidates(HistoryNotificationEntity... configs) {
        when(repository.findByHistoryTypeIdInAndTwinClassIdInAndNotificationSchemaIdInAndActiveTrue(
                anyCollection(), anyCollection(), anyCollection()))
                .thenReturn(List.of(configs));
    }

    // ---------- scenarios ----------

    @Nested
    class Matching {

        @Test
        void matchesByExactClassAndByExtendedChain() {
            var parentClassId = UUID.randomUUID();
            var unrelatedClassId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var configOnParent = config(HistoryType.twinCreated, schemaId, parentClassId, null);
            var configOnUnrelated = config(HistoryType.twinCreated, schemaId, unrelatedClassId, null);
            candidates(configOnParent, configOnUnrelated);
            // twin of a child class inheriting from parent → config on parent matches via extended chain
            var childClass = twinClass(UUID.randomUUID(), parentClassId);
            var taskEntity = task(HistoryType.twinCreated, schemaId, childClass, null);
            var chunk = chunkOf(taskEntity);

            assertDoesNotThrow(() -> service.findConfigsForTasks(chunk));

            assertEquals(List.of(configOnParent), chunk.getConfigsByTask().get(taskEntity));
            assertTrue(chunk.getTasksByConfig().get(configOnParent).contains(taskEntity));
            assertNull(chunk.getTasksByConfig().get(configOnUnrelated));
        }

        @Test
        void historyTypeOrSchemaMismatch_notMatched() {
            var classId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var configOtherType = config(HistoryType.statusChanged, schemaId, classId, null);
            var configOtherSchema = config(HistoryType.twinCreated, UUID.randomUUID(), classId, null);
            candidates(configOtherType, configOtherSchema);
            var taskEntity = task(HistoryType.twinCreated, schemaId, twinClass(classId), null);
            var chunk = chunkOf(taskEntity);

            assertDoesNotThrow(() -> service.findConfigsForTasks(chunk));

            assertTrue(chunk.getConfigsByTask().isEmpty());
            assertTrue(chunk.getTasksByConfig().isEmpty());
        }

        @Test
        void taskFieldIdNull_matchesConfigsWithAnyFieldId() {
            var classId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var fieldX = UUID.randomUUID();
            var configNoField = config(HistoryType.twinCreated, schemaId, classId, null);
            var configFieldX = config(HistoryType.twinCreated, schemaId, classId, fieldX);
            candidates(configNoField, configFieldX);
            var taskEntity = task(HistoryType.twinCreated, schemaId, twinClass(classId), null); // fieldId == null
            var chunk = chunkOf(taskEntity);

            assertDoesNotThrow(() -> service.findConfigsForTasks(chunk));

            assertEquals(2, chunk.getConfigsByTask().get(taskEntity).size());
        }

        @Test
        void taskFieldIdNonNull_matchesOnlyExactFieldId() {
            var classId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var fieldX = UUID.randomUUID();
            var fieldY = UUID.randomUUID();
            var configNoField = config(HistoryType.twinCreated, schemaId, classId, null);
            var configFieldX = config(HistoryType.twinCreated, schemaId, classId, fieldX);
            var configFieldY = config(HistoryType.twinCreated, schemaId, classId, fieldY);
            candidates(configNoField, configFieldX, configFieldY);
            var taskEntity = task(HistoryType.twinCreated, schemaId, twinClass(classId), fieldX);
            var chunk = chunkOf(taskEntity);

            assertDoesNotThrow(() -> service.findConfigsForTasks(chunk));

            assertEquals(List.of(configFieldX), chunk.getConfigsByTask().get(taskEntity));
        }

        @Test
        void taskWithoutHistoryChain_skippedEntirely() {
            // history without twinClass → no matching context, no repo call for this task
            var taskEntity = new HistoryNotificationTaskEntity().setId(UUID.randomUUID());
            var chunk = chunkOf(taskEntity);

            assertDoesNotThrow(() -> service.findConfigsForTasks(chunk));

            assertTrue(chunk.getConfigsByTask().isEmpty());
            assertTrue(chunk.getTasksByConfig().isEmpty());
        }
    }

    @Nested
    class Validation {

        @Test
        void invalidTwinExcludedFromBothProjections() throws Exception {
            var classId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var configMatched = config(HistoryType.twinCreated, schemaId, classId, null);
            candidates(configMatched);
            var taskValid = task(HistoryType.twinCreated, schemaId, twinClass(classId), null);
            var taskInvalid = task(HistoryType.twinCreated, schemaId, twinClass(classId), null);
            var chunk = chunkOf(taskValid, taskInvalid);
            var invalidTwinId = taskInvalid.getHistory().getTwin().getId();
            when(twinValidatorSetService.isValid(org.mockito.ArgumentMatchers.<Collection<TwinEntity>>any(), eq(configMatched))).thenReturn(Map.of(
                    taskValid.getHistory().getTwin().getId(), ValidationResult.VALID,
                    invalidTwinId, new ValidationResult(false, "does not pass the set")));

            service.findConfigsForTasks(chunk);

            // both projections agree: only the valid task stays with the config
            assertEquals(List.of(configMatched), chunk.getConfigsByTask().get(taskValid));
            assertNull(chunk.getConfigsByTask().get(taskInvalid));
            assertEquals(Set.of(taskValid), chunk.getTasksByConfig().get(configMatched));
        }

        @Test
        void allTasksInvalid_configDroppedFromProjections() throws Exception {
            var classId = UUID.randomUUID();
            var schemaId = UUID.randomUUID();
            var configMatched = config(HistoryType.twinCreated, schemaId, classId, null);
            candidates(configMatched);
            var taskEntity = task(HistoryType.twinCreated, schemaId, twinClass(classId), null);
            var chunk = chunkOf(taskEntity);
            when(twinValidatorSetService.isValid(org.mockito.ArgumentMatchers.<Collection<TwinEntity>>any(), eq(configMatched))).thenReturn(Map.of(
                    taskEntity.getHistory().getTwin().getId(), new ValidationResult(false)));

            service.findConfigsForTasks(chunk);

            assertTrue(chunk.getConfigsByTask().isEmpty());
            assertTrue(chunk.getTasksByConfig().isEmpty());
        }
    }
}
