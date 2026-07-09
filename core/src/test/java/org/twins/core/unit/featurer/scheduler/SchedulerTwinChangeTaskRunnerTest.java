package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
import org.twins.core.featurer.scheduler.tasks.TwinChangeTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SchedulerTwinChangeTaskRunnerTest extends BaseUnitTest {

    @Mock
    private TwinChangeTaskRepository twinChangeTaskRepository;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ApplicationContext applicationContext;

    private SchedulerTwinChangeTaskRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        runner = new SchedulerTwinChangeTaskRunner(taskExecutor, twinChangeTaskRepository);
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

    private TwinChangeTaskEntity buildEntity() {
        return new TwinChangeTaskEntity();
    }

    @Nested
    class GetTaskClass {

        @Test
        void getTaskClass_returnsTwinChangeTask() {
            assertEquals(TwinChangeTask.class, runner.getTaskClass());
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectAll_delegatesToRepository() {
            var entities = List.of(buildEntity());
            when(twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START)))
                    .thenReturn(entities);

            var result = runner.collectAll();

            assertEquals(1, result.size());
        }

        @Test
        void collectAll_returnsEmptyList() {
            when(twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START)))
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
            when(twinChangeTaskRepository.findByStatusIdIn(
                    eq(List.of(TwinChangeTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);

            var result = runner.collectBatch(10);

            assertEquals(1, result.size());
            verify(twinChangeTaskRepository).findByStatusIdIn(
                    eq(List.of(TwinChangeTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }
    }

    @Nested
    class SetStatusAndSave {

        @Test
        void setStatusAndSave_setsInProgressAndSaves() {
            var entity = buildEntity();
            var entities = List.of(entity);
            when(twinChangeTaskRepository.saveAll(entities)).thenReturn(entities);

            var result = new ArrayList<>(runner.setStatusAndSave(entities));

            assertEquals(TwinChangeTaskStatus.IN_PROGRESS, entity.getStatusId());
            verify(twinChangeTaskRepository).saveAll(entities);
        }

        @Test
        void setStatusAndSave_handlesMultipleEntities() {
            var entity1 = buildEntity();
            var entity2 = buildEntity();
            var entities = List.of(entity1, entity2);
            when(twinChangeTaskRepository.saveAll(entities)).thenReturn(entities);

            runner.setStatusAndSave(entities);

            assertEquals(TwinChangeTaskStatus.IN_PROGRESS, entity1.getStatusId());
            assertEquals(TwinChangeTaskStatus.IN_PROGRESS, entity2.getStatusId());
        }

        @Test
        void setStatusAndSave_statusSetBeforeSaveAll() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var captor = ArgumentCaptor.forClass(Collection.class);
            when(twinChangeTaskRepository.saveAll(captor.capture())).thenReturn(entities);

            runner.setStatusAndSave(entities);

            var savedEntities = captor.getValue();
            assertTrue(savedEntities.stream()
                    .allMatch(e -> ((TwinChangeTaskEntity) e).getStatusId() == TwinChangeTaskStatus.IN_PROGRESS));
        }
    }

    @Nested
    class RevertStatusBatch {

        @Test
        void revertStatusBatch_revertsToNeedStartAndSaves() {
            var entity = buildEntity();
            entity.setStatusId(TwinChangeTaskStatus.IN_PROGRESS);

            runner.revertStatusAndSave(List.of(entity));

            assertEquals(TwinChangeTaskStatus.NEED_START, entity.getStatusId());
            verify(twinChangeTaskRepository).saveAll(List.of(entity));
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noTasksCollected_returnsEmptyString() {
            when(twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.processTask(new Properties());

            assertEquals("", result);
            verify(taskExecutor, never()).execute(any());
        }

        @Test
        void processTask_withTasks_processesAndReturnsCount() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(TwinChangeTask.class);

            when(twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START)))
                    .thenReturn(entities);
            when(twinChangeTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(TwinChangeTask.class, entity)).thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            verify(applicationContext).getBean(TwinChangeTask.class, entity);
            verify(taskExecutor).execute(task);
            assertEquals(TwinChangeTaskStatus.IN_PROGRESS, entity.getStatusId());
        }

        @Test
        void processTask_withBatchSize_processesBatch() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(TwinChangeTask.class);
            var props = new Properties();
            props.put("batchSize", "10");

            when(twinChangeTaskRepository.findByStatusIdIn(
                    eq(List.of(TwinChangeTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);
            when(twinChangeTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(TwinChangeTask.class, entity)).thenReturn(task);

            var result = runner.processTask(props);

            assertEquals("1 task(s) from db was processed", result);
            verify(twinChangeTaskRepository).findByStatusIdIn(
                    eq(List.of(TwinChangeTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void processTask_exceptionInCollectTasks_returnsErrorMessage() {
            when(twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START)))
                    .thenThrow(new RuntimeException("DB error"));

            var result = runner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
        }
    }
}
