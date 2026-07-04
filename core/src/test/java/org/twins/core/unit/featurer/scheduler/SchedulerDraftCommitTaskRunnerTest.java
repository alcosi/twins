package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftRepository;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.featurer.scheduler.tasks.DraftCommitTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerDraftCommitTaskRunnerTest extends BaseUnitTest {

    @Mock
    private DraftRepository draftRepository;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ApplicationContext applicationContext;

    private SchedulerDraftCommitTaskRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        runner = new SchedulerDraftCommitTaskRunner(taskExecutor, draftRepository);
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

    private DraftEntity buildEntity() {
        return new DraftEntity();
    }

    @Nested
    class GetTaskClass {

        @Test
        void getTaskClass_returnsDraftCommitTask() {
            assertEquals(DraftCommitTask.class, runner.getTaskClass());
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectAll_delegatesToRepository() {
            var entities = List.of(buildEntity());
            when(draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true))
                    .thenReturn(entities);

            var result = runner.collectAll();

            assertEquals(1, result.size());
        }

        @Test
        void collectAll_returnsEmptyList() {
            when(draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true))
                    .thenReturn(Collections.emptyList());

            var result = runner.collectAll();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CollectBatch {

        @Test
        void collectBatch_delegatesToRepositoryWithPageable() {
            var entities = List.of(buildEntity());
            when(draftRepository.findByStatusInAndAutoCommit(
                    eq(List.of(DraftStatus.UNCOMMITED)), eq(true), any(Pageable.class)))
                    .thenReturn(entities);

            var result = runner.collectBatch(5);

            assertEquals(1, result.size());
            verify(draftRepository).findByStatusInAndAutoCommit(
                    eq(List.of(DraftStatus.UNCOMMITED)), eq(true),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 5));
        }
    }

    @Nested
    class SetStatusAndSave {

        @Test
        void setStatusAndSave_setsCommitInProgressAndSaves() {
            var entity = buildEntity();
            var entities = List.of(entity);
            when(draftRepository.saveAll(entities)).thenReturn(entities);

            var result = new ArrayList<>(runner.setStatusAndSave(entities));

            assertEquals(DraftStatus.COMMIT_IN_PROGRESS, entity.getStatus());
            verify(draftRepository).saveAll(entities);
        }

        @Test
        void setStatusAndSave_handlesMultipleEntities() {
            var entity1 = buildEntity();
            var entity2 = buildEntity();
            var entities = List.of(entity1, entity2);
            when(draftRepository.saveAll(entities)).thenReturn(entities);

            runner.setStatusAndSave(entities);

            assertEquals(DraftStatus.COMMIT_IN_PROGRESS, entity1.getStatus());
            assertEquals(DraftStatus.COMMIT_IN_PROGRESS, entity2.getStatus());
        }

        @Test
        void setStatusAndSave_statusSetBeforeSaveAll() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var captor = ArgumentCaptor.forClass(Collection.class);
            when(draftRepository.saveAll(captor.capture())).thenReturn(entities);

            runner.setStatusAndSave(entities);

            var savedEntities = captor.getValue();
            assertTrue(savedEntities.stream()
                    .allMatch(e -> ((DraftEntity) e).getStatus() == DraftStatus.COMMIT_IN_PROGRESS));
        }
    }

    @Nested
    class RevertStatusAndSave {

        @Test
        void revertStatusAndSave_revertsToUncommitedAndSaves() {
            var entity = buildEntity();
            entity.setStatus(DraftStatus.COMMIT_IN_PROGRESS);

            runner.revertStatusAndSave(entity);

            assertEquals(DraftStatus.UNCOMMITED, entity.getStatus());
            verify(draftRepository).save(entity);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noTasksCollected_returnsEmptyString() {
            when(draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true))
                    .thenReturn(Collections.emptyList());

            var result = runner.processTask(new Properties());

            assertEquals("", result);
            verify(taskExecutor, never()).execute(any());
        }

        @Test
        void processTask_withTasks_processesAndReturnsCount() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(DraftCommitTask.class);

            when(draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true))
                    .thenReturn(entities);
            when(draftRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(DraftCommitTask.class, entity)).thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            verify(applicationContext).getBean(DraftCommitTask.class, entity);
            verify(taskExecutor).execute(task);
            assertEquals(DraftStatus.COMMIT_IN_PROGRESS, entity.getStatus());
        }

        @Test
        void processTask_withBatchSize_processesBatch() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(DraftCommitTask.class);
            var props = new Properties();
            props.put("batchSize", "10");

            when(draftRepository.findByStatusInAndAutoCommit(
                    eq(List.of(DraftStatus.UNCOMMITED)), eq(true), any(Pageable.class)))
                    .thenReturn(entities);
            when(draftRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(DraftCommitTask.class, entity)).thenReturn(task);

            var result = runner.processTask(props);

            assertEquals("1 task(s) from db was processed", result);
            verify(draftRepository).findByStatusInAndAutoCommit(
                    eq(List.of(DraftStatus.UNCOMMITED)), eq(true),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void processTask_exceptionInCollectTasks_returnsErrorMessage() {
            when(draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true))
                    .thenThrow(new RuntimeException("DB error"));

            var result = runner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
        }
    }
}
