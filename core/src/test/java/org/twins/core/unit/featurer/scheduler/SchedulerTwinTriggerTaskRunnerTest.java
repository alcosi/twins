package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.featurer.scheduler.tasks.TwinTriggerTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerTwinTriggerTaskRunnerTest extends BaseUnitTest {

    @Mock
    private TwinTriggerTaskRepository twinTriggerTaskRepository;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ApplicationContext applicationContext;

    private SchedulerTwinTriggerTaskRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        runner = new SchedulerTwinTriggerTaskRunner(taskExecutor, twinTriggerTaskRepository);
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

    private TwinTriggerTaskEntity buildEntity() {
        return new TwinTriggerTaskEntity();
    }

    @Nested
    class GetTaskClass {

        @Test
        void getTaskClass_returnsTwinTriggerTask() {
            assertEquals(TwinTriggerTask.class, runner.getTaskClass());
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectAll_delegatesToRepository() {
            var entities = List.of(buildEntity());
            when(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)))
                    .thenReturn(entities);

            var result = runner.collectAll();

            assertEquals(1, result.size());
        }

        @Test
        void collectAll_returnsEmptyList() {
            when(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)))
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
            Page<TwinTriggerTaskEntity> page = new PageImpl<>(entities);
            when(twinTriggerTaskRepository.findByStatusIdIn(
                    eq(List.of(TwinTriggerTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(page);

            var result = runner.collectBatch(10);

            assertEquals(1, result.size());
            verify(twinTriggerTaskRepository).findByStatusIdIn(
                    eq(List.of(TwinTriggerTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void collectBatch_returnsEmptyList() {
            Page<TwinTriggerTaskEntity> emptyPage = new PageImpl<>(Collections.emptyList());
            when(twinTriggerTaskRepository.findByStatusIdIn(
                    eq(List.of(TwinTriggerTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(emptyPage);

            var result = runner.collectBatch(10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class SetStatusAndSave {

        @Test
        void setStatusAndSave_setsInProgressAndSaves() {
            var entity = buildEntity();
            var entities = List.of(entity);
            when(twinTriggerTaskRepository.saveAll(entities)).thenReturn(entities);

            var result = new ArrayList<>(runner.setStatusAndSave(entities));

            assertEquals(TwinTriggerTaskStatus.IN_PROGRESS, entity.getStatusId());
            verify(twinTriggerTaskRepository).saveAll(entities);
        }

        @Test
        void setStatusAndSave_handlesMultipleEntities() {
            var entity1 = buildEntity();
            var entity2 = buildEntity();
            var entities = List.of(entity1, entity2);
            when(twinTriggerTaskRepository.saveAll(entities)).thenReturn(entities);

            runner.setStatusAndSave(entities);

            assertEquals(TwinTriggerTaskStatus.IN_PROGRESS, entity1.getStatusId());
            assertEquals(TwinTriggerTaskStatus.IN_PROGRESS, entity2.getStatusId());
        }

        @Test
        void setStatusAndSave_statusSetBeforeSaveAll() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var captor = ArgumentCaptor.forClass(Collection.class);
            when(twinTriggerTaskRepository.saveAll(captor.capture())).thenReturn(entities);

            runner.setStatusAndSave(entities);

            var savedEntities = captor.getValue();
            assertTrue(savedEntities.stream()
                    .allMatch(e -> ((TwinTriggerTaskEntity) e).getStatusId() == TwinTriggerTaskStatus.IN_PROGRESS));
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noTasksCollected_returnsEmptyString() {
            when(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.processTask(new Properties());

            assertEquals("", result);
            verify(taskExecutor, never()).execute(any());
        }

        @Test
        void processTask_withTasks_processesAndReturnsCount() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(TwinTriggerTask.class);

            when(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)))
                    .thenReturn(entities);
            when(twinTriggerTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(TwinTriggerTask.class, entity)).thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            verify(applicationContext).getBean(TwinTriggerTask.class, entity);
            verify(taskExecutor).execute(task);
            assertEquals(TwinTriggerTaskStatus.IN_PROGRESS, entity.getStatusId());
        }

        @Test
        void processTask_withBatchSize_processesBatch() {
            var entity = buildEntity();
            var entities = List.of(entity);
            var task = mock(TwinTriggerTask.class);
            var props = new Properties();
            props.put("batchSize", "10");

            Page<TwinTriggerTaskEntity> page = new PageImpl<>(entities);
            when(twinTriggerTaskRepository.findByStatusIdIn(
                    eq(List.of(TwinTriggerTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(page);
            when(twinTriggerTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(TwinTriggerTask.class, entity)).thenReturn(task);

            var result = runner.processTask(props);

            assertEquals("1 task(s) from db was processed", result);
            verify(twinTriggerTaskRepository).findByStatusIdIn(
                    eq(List.of(TwinTriggerTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void processTask_exceptionInCollectTasks_returnsErrorMessage() {
            when(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)))
                    .thenThrow(new RuntimeException("DB error"));

            var result = runner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
        }
    }
}
