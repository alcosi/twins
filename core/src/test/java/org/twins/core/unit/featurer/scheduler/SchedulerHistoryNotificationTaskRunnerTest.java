package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.scheduler.tasks.HistoryNotificationTask;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.notification.HistoryNotificationChunk;
import org.twins.core.service.notification.HistoryNotificationTaskService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerHistoryNotificationTaskRunnerTest extends BaseUnitTest {

    @Mock
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private HistoryService historyService;

    @Mock
    private HistoryNotificationTaskService historyNotificationTaskService;

    private SchedulerHistoryNotificationTaskRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        runner = new SchedulerHistoryNotificationTaskRunner(
                taskExecutor, historyNotificationTaskRepository, historyService, historyNotificationTaskService);
        setField(runner, "applicationContext", applicationContext);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    /** Task entity whose history chain resolves to the given domain (history → twin → twinClass → domainId). */
    private HistoryNotificationTaskEntity buildEntity(UUID domainId) {
        TwinClassEntity twinClass = new TwinClassEntity().setDomainId(domainId);
        TwinEntity twin = new TwinEntity().setTwinClass(twinClass);
        HistoryEntity history = new HistoryEntity().setTwin(twin);
        return new HistoryNotificationTaskEntity().setHistory(history);
    }

    @Nested
    class GetTaskClass {

        @Test
        void getTaskClass_returnsHistoryNotificationTask() {
            assertEquals(HistoryNotificationTask.class, runner.getTaskClass());
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectAll_delegatesToRepository() {
            var entities = List.of(buildEntity(null));
            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(entities);

            var result = runner.collectAll();

            assertEquals(1, result.size());
        }

        @Test
        void collectAll_returnsEmptyList() {
            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.collectAll();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CollectBatch {

        @Test
        void collectBatch_delegatesToRepositoryWithPageable() {
            var entities = List.of(buildEntity(null));
            when(historyNotificationTaskRepository.findByStatusIdIn(
                    eq(List.of(HistoryNotificationTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);

            var result = runner.collectBatch(20);

            assertEquals(1, result.size());
            verify(historyNotificationTaskRepository).findByStatusIdIn(
                    eq(List.of(HistoryNotificationTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 20));
        }
    }

    @Nested
    class SetStatusAndSave {

        @Test
        void setStatusAndSave_setsInProgressAndBulkUpdates() throws Exception {
            var entity = buildEntity(null);
            var entities = List.of(entity);

            var result = new ArrayList<>(runner.setStatusAndSave(entities));

            assertEquals(HistoryNotificationTaskStatus.IN_PROGRESS, entity.getStatusId());
            verify(historyNotificationTaskService).loadHistory(entities);
            verify(historyService).loadUser(entities.stream().map(HistoryNotificationTaskEntity::getHistory).toList());
            // regression guard (TWINS-836 review P1): twins in the scheduler thread must go through the
            // unsafe load — the secure path hits AuthService.getApiUser on a request-scoped proxy and throws
            verify(historyService).loadTwinUnsafe(entities.stream().map(HistoryNotificationTaskEntity::getHistory).toList());
            verify(historyService, never()).loadTwin(anyList());
            verify(historyNotificationTaskService).updateStatuses(entities);
        }

        @Test
        void setStatusAndSave_handlesMultipleEntities() {
            var entity1 = buildEntity(null);
            var entity2 = buildEntity(null);
            var entities = List.of(entity1, entity2);

            runner.setStatusAndSave(entities);

            assertEquals(HistoryNotificationTaskStatus.IN_PROGRESS, entity1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.IN_PROGRESS, entity2.getStatusId());
        }

        @Test
        void setStatusAndSave_statusSetBeforeBulkUpdate() {
            var entity = buildEntity(null);
            var entities = List.of(entity);
            var captor = ArgumentCaptor.forClass(Collection.class);

            runner.setStatusAndSave(entities);

            verify(historyNotificationTaskService).updateStatuses(captor.capture());
            var capturedEntities = captor.getValue();
            assertTrue(capturedEntities.stream()
                    .allMatch(e -> ((HistoryNotificationTaskEntity) e).getStatusId() == HistoryNotificationTaskStatus.IN_PROGRESS));
        }
    }

    @Nested
    class RevertStatusBatch {

        @Test
        void revertStatusBatch_revertsToNeedStartAndSaves() {
            var entity = buildEntity(null);
            entity.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS);

            runner.revertStatusAndSave(List.of(entity));

            assertEquals(HistoryNotificationTaskStatus.NEED_START, entity.getStatusId());
            assertEquals(1, entity.getAttemptCount());
            assertTrue(entity.getStatusDetails().contains("attempt 1 of " + HistoryNotificationTask.MAX_BATCH_ATTEMPTS));
            assertNull(entity.getDoneAt());
            verify(historyNotificationTaskService).updateStatuses(List.of(entity));
        }

        @Test
        void revertStatusBatch_afterMaxAttempts_escalatesToFailed() {
            var entity = buildEntity(null);
            entity.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS);
            entity.setAttemptCount(2); // two submission rejections already recorded

            runner.revertStatusAndSave(List.of(entity));

            assertEquals(HistoryNotificationTaskStatus.FAILED, entity.getStatusId());
            assertEquals(3, entity.getAttemptCount());
            assertNotNull(entity.getDoneAt());
            verify(historyNotificationTaskService).updateStatuses(List.of(entity));
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noTasksCollected_returnsEmptyString() {
            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.processTask(new Properties());

            assertEquals("", result);
            verify(taskExecutor, never()).execute(any());
        }

        @Test
        void processTask_withTasks_submitsOneBatchPerChunk() {
            UUID domainId = UUID.randomUUID();
            var entity = buildEntity(domainId);
            var entities = List.of(entity);
            var task = mock(HistoryNotificationTask.class);

            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(entities);
            // bulk status update goes through historyNotificationTaskService.updateStatuses (void) — no stub needed
            when(applicationContext.getBean(eq(HistoryNotificationTask.class), any(HistoryNotificationChunk.class)))
                    .thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            var chunkCaptor = ArgumentCaptor.forClass(HistoryNotificationChunk.class);
            verify(applicationContext).getBean(eq(HistoryNotificationTask.class), chunkCaptor.capture());
            verify(taskExecutor).execute(task);
            assertEquals(domainId, chunkCaptor.getValue().getDomainId());
            assertEquals(entities, chunkCaptor.getValue().getTasks());
            assertEquals(HistoryNotificationTaskStatus.IN_PROGRESS, entity.getStatusId());
        }

        @Test
        void processTask_groupsByDomain_neverMixesDomainsInChunk() {
            UUID domainA = UUID.randomUUID();
            UUID domainB = UUID.randomUUID();
            var entityA1 = buildEntity(domainA);
            var entityA2 = buildEntity(domainA);
            var entityB = buildEntity(domainB);
            var entities = List.of(entityA1, entityA2, entityB);
            var task = mock(HistoryNotificationTask.class);

            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(entities);
            // bulk status update goes through historyNotificationTaskService.updateStatuses (void) — no stub needed
            when(applicationContext.getBean(eq(HistoryNotificationTask.class), any(HistoryNotificationChunk.class)))
                    .thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("3 task(s) from db was processed", result);
            var chunkCaptor = ArgumentCaptor.forClass(HistoryNotificationChunk.class);
            // one domain bucket may be split into several chunks, but every chunk carries exactly one domainId
            verify(applicationContext, times(2)).getBean(eq(HistoryNotificationTask.class), chunkCaptor.capture());
            for (HistoryNotificationChunk chunk : chunkCaptor.getAllValues()) {
                if (chunk.getDomainId().equals(domainA)) {
                    assertEquals(new HashSet<>(List.of(entityA1, entityA2)), new HashSet<>(chunk.getTasks()));
                } else {
                    assertEquals(domainB, chunk.getDomainId());
                    assertEquals(List.of(entityB), chunk.getTasks());
                }
            }
            verify(taskExecutor, times(2)).execute(task);
        }

        @Test
        void processTask_nullDomain_skippedWithoutDispatch() {
            var entity = buildEntity(null); // no domain resolvable from the history chain
            var entities = List.of(entity);

            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(entities);
            // bulk status update goes through historyNotificationTaskService.updateStatuses (void) — no stub needed

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            assertEquals(HistoryNotificationTaskStatus.SKIPPED, entity.getStatusId());
            assertEquals("Twin is out of domain", entity.getStatusDetails());
            verify(applicationContext, never()).getBean(eq(HistoryNotificationTask.class), any(HistoryNotificationChunk.class));
            verify(taskExecutor, never()).execute(any());
            // saved twice: once as IN_PROGRESS by setStatusAndSave, once as SKIPPED by the no-domain branch
            verify(historyNotificationTaskService, times(2)).updateStatuses(entities);
        }

        @Test
        void processTask_chunkSubmissionRejected_revertsStatus() {
            UUID domainId = UUID.randomUUID();
            var entity = buildEntity(domainId);
            var entities = List.of(entity);

            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenReturn(entities);
            // bulk status update goes through historyNotificationTaskService.updateStatuses (void) — no stub needed
            when(applicationContext.getBean(eq(HistoryNotificationTask.class), any(HistoryNotificationChunk.class)))
                    .thenThrow(new RuntimeException("executor saturated"));

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            assertEquals(HistoryNotificationTaskStatus.NEED_START, entity.getStatusId());
            verify(taskExecutor, never()).execute(any());
            // saved twice: once as IN_PROGRESS by setStatusAndSave, once reverted to NEED_START
            verify(historyNotificationTaskService, times(2)).updateStatuses(List.of(entity));
        }

        @Test
        void processTask_withBatchSize_processesBatch() {
            UUID domainId = UUID.randomUUID();
            var entity = buildEntity(domainId);
            var entities = List.of(entity);
            var task = mock(HistoryNotificationTask.class);
            var props = new Properties();
            props.put("batchSize", "10");

            when(historyNotificationTaskRepository.findByStatusIdIn(
                    eq(List.of(HistoryNotificationTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);
            // bulk status update goes through historyNotificationTaskService.updateStatuses (void) — no stub needed
            when(applicationContext.getBean(eq(HistoryNotificationTask.class), any(HistoryNotificationChunk.class)))
                    .thenReturn(task);

            var result = runner.processTask(props);

            assertEquals("1 task(s) from db was processed", result);
            verify(historyNotificationTaskRepository).findByStatusIdIn(
                    eq(List.of(HistoryNotificationTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void processTask_exceptionInCollectTasks_returnsErrorMessage() {
            when(historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START)))
                    .thenThrow(new RuntimeException("DB error"));

            var result = runner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
        }
    }
}
