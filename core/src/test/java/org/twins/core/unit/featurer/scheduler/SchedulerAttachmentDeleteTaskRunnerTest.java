package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.AttachmentDeleteTaskEntity;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.scheduler.tasks.AttachmentDeleteTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerAttachmentDeleteTaskRunnerTest extends BaseUnitTest {

    @Mock
    private AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ApplicationContext applicationContext;

    private SchedulerAttachmentDeleteTaskRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        runner = new SchedulerAttachmentDeleteTaskRunner(taskExecutor, attachmentDeleteTaskRepository);
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

    private AttachmentDeleteTaskEntity buildEntity(UUID id) {
        var entity = new AttachmentDeleteTaskEntity();
        return entity;
    }

    @Nested
    class GetTaskClass {

        @Test
        void getTaskClass_returnsAttachmentDeleteTask() {
            assertEquals(AttachmentDeleteTask.class, runner.getTaskClass());
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectAll_delegatesToRepository() {
            var entities = List.of(buildEntity(UUID.randomUUID()));
            when(attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START)))
                    .thenReturn(entities);

            var result = runner.collectAll();

            assertEquals(1, result.size());
        }

        @Test
        void collectAll_returnsEmptyList() {
            when(attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.collectAll();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CollectBatch {

        @Test
        void collectBatch_delegatesToRepositoryWithPageable() {
            var entities = List.of(buildEntity(UUID.randomUUID()));
            when(attachmentDeleteTaskRepository.findByStatusIn(
                    eq(List.of(AttachmentDeleteTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);

            var result = runner.collectBatch(10);

            assertEquals(1, result.size());
            verify(attachmentDeleteTaskRepository).findByStatusIn(
                    eq(List.of(AttachmentDeleteTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }
    }

    @Nested
    class SetStatusAndSave {

        @Test
        void setStatusAndSave_setsInProgressAndSaves() {
            var entity = buildEntity(UUID.randomUUID());
            var entities = List.of(entity);
            when(attachmentDeleteTaskRepository.saveAll(entities)).thenReturn(entities);

            var result = new ArrayList<>(runner.setStatusAndSave(entities));

            assertEquals(AttachmentDeleteTaskStatus.IN_PROGRESS, entity.getStatus());
            verify(attachmentDeleteTaskRepository).saveAll(entities);
        }

        @Test
        void setStatusAndSave_handlesMultipleEntities() {
            var entity1 = buildEntity(UUID.randomUUID());
            var entity2 = buildEntity(UUID.randomUUID());
            var entities = List.of(entity1, entity2);
            when(attachmentDeleteTaskRepository.saveAll(entities)).thenReturn(entities);

            runner.setStatusAndSave(entities);

            assertEquals(AttachmentDeleteTaskStatus.IN_PROGRESS, entity1.getStatus());
            assertEquals(AttachmentDeleteTaskStatus.IN_PROGRESS, entity2.getStatus());
        }

        @Test
        void setStatusAndSave_statusSetBeforeSaveAll() {
            var entity = buildEntity(UUID.randomUUID());
            var entities = List.of(entity);
            var captor = ArgumentCaptor.forClass(Collection.class);
            when(attachmentDeleteTaskRepository.saveAll(captor.capture())).thenReturn(entities);

            runner.setStatusAndSave(entities);

            var savedEntities = captor.getValue();
            assertTrue(savedEntities.stream()
                    .allMatch(e -> ((AttachmentDeleteTaskEntity) e).getStatus() == AttachmentDeleteTaskStatus.IN_PROGRESS));
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noTasksCollected_returnsEmptyString() {
            when(attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START)))
                    .thenReturn(Collections.emptyList());

            var result = runner.processTask(new Properties());

            assertEquals("", result);
            verify(taskExecutor, never()).execute(any());
        }

        @Test
        void processTask_withTasks_processesAndReturnsCount() {
            var entity = buildEntity(UUID.randomUUID());
            var entities = List.of(entity);
            var task = mock(AttachmentDeleteTask.class);

            when(attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START)))
                    .thenReturn(entities);
            when(attachmentDeleteTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(AttachmentDeleteTask.class, entity)).thenReturn(task);

            var result = runner.processTask(new Properties());

            assertEquals("1 task(s) from db was processed", result);
            verify(applicationContext).getBean(AttachmentDeleteTask.class, entity);
            verify(taskExecutor).execute(task);
            assertEquals(AttachmentDeleteTaskStatus.IN_PROGRESS, entity.getStatus());
        }

        @Test
        void processTask_withBatchSize_processesBatch() {
            var entity = buildEntity(UUID.randomUUID());
            var entities = List.of(entity);
            var task = mock(AttachmentDeleteTask.class);
            var props = new Properties();
            props.put("batchSize", "10");

            when(attachmentDeleteTaskRepository.findByStatusIn(
                    eq(List.of(AttachmentDeleteTaskStatus.NEED_START)), any(Pageable.class)))
                    .thenReturn(entities);
            when(attachmentDeleteTaskRepository.saveAll(entities)).thenReturn(entities);
            when(applicationContext.getBean(AttachmentDeleteTask.class, entity)).thenReturn(task);

            var result = runner.processTask(props);

            assertEquals("1 task(s) from db was processed", result);
            verify(attachmentDeleteTaskRepository).findByStatusIn(
                    eq(List.of(AttachmentDeleteTaskStatus.NEED_START)),
                    argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
        }

        @Test
        void processTask_exceptionInCollectTasks_returnsErrorMessage() {
            when(attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START)))
                    .thenThrow(new RuntimeException("DB error"));

            var result = runner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
        }
    }
}
