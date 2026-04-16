package org.twins.core.service.trigger;

import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.trigger.TwinTrigger;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinService.TwinCreateResult;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinTriggerServiceJobTwinTest {

    private static final int TEST_TRIGGER_FEATURER_ID = 1503;

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private TwinTriggerTaskService twinTriggerTaskService;

    @InjectMocks
    private TwinTriggerService twinTriggerService;

    private TwinEntity twinEntity;
    private TwinStatusEntity srcStatus;
    private TwinStatusEntity dstStatus;
    private TwinClassEntity jobTwinClass;

    @BeforeEach
    void setUp() throws Exception {
        twinEntity = new TwinEntity()
                .setId(UUID.randomUUID())
                .setTwinClassId(SystemEntityService.TWIN_CLASS_USER);

        srcStatus = new TwinStatusEntity()
                .setId(SystemEntityService.TWIN_STATUS_USER)
                .setTwinClassId(SystemEntityService.TWIN_CLASS_USER);

        dstStatus = new TwinStatusEntity()
                .setId(SystemEntityService.TWIN_STATUS_USER)
                .setTwinClassId(SystemEntityService.TWIN_CLASS_USER);

        UUID jobTwinClassId = UuidUtils.generate();
        jobTwinClass = new TwinClassEntity()
                .setId(jobTwinClassId)
                .setKey("JOB");

        lenient().when(twinClassService.findEntitySafe(jobTwinClassId)).thenReturn(jobTwinClass);
    }

    private TwinTriggerEntity createTriggerEntityWithJobClass(UUID jobTwinClassId) {
        return new TwinTriggerEntity()
                .setId(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setTwinTriggerFeaturerId(TEST_TRIGGER_FEATURER_ID)
                .setJobTwinClassId(jobTwinClassId)
                .setJobTwinClass(jobTwinClass)
                .setTwinTriggerParam(new HashMap<>());
    }

    private TwinTriggerEntity createTriggerEntityWithoutJobClass() {
        return new TwinTriggerEntity()
                .setId(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setTwinTriggerFeaturerId(TEST_TRIGGER_FEATURER_ID)
                .setJobTwinClassId(null)
                .setJobTwinClass(null)
                .setTwinTriggerParam(new HashMap<>());
    }

    private TwinCreateResult createTwinCreateResult(TwinEntity createdTwin) {
        return new TwinCreateResult().setCreatedTwin(createdTwin);
    }

    @Test
    void shouldCreateJobTwinWithTaskId_Async() throws Exception {
        // given
        UUID taskId = UuidUtils.generate();
        UUID jobTwinClassId = jobTwinClass.getId();
        TwinTriggerEntity triggerEntity = createTriggerEntityWithJobClass(jobTwinClassId);

        TwinTrigger mockTrigger = mock(TwinTrigger.class);
        Properties triggerProps = new Properties();

        lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any())).thenReturn(triggerProps);
        lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class))).thenReturn(mockTrigger);
        when(twinService.createTwin(any(TwinCreate.class))).thenReturn(createTwinCreateResult(new TwinEntity()));

        // when
        twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus, taskId);

        // then
        ArgumentCaptor<TwinCreate> captor = ArgumentCaptor.forClass(TwinCreate.class);
        verify(twinService).createTwin(captor.capture());

        TwinCreate capturedCreate = captor.getValue();
        assertEquals(taskId, capturedCreate.getTwinEntity().getId(), "Job twin ID should match task ID");
        assertEquals(jobTwinClassId, capturedCreate.getTwinEntity().getTwinClassId(), "Job twin class ID should match");
        assertEquals(org.twins.core.domain.twinoperation.TwinOperation.Launcher.trigger, capturedCreate.getLauncher(), "Launcher should be trigger");
        assertFalse(capturedCreate.isCanTriggerAfterOperationFactory(), "Should not trigger after operation factory");

        // Verify trigger was executed with correct task ID
        verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), eq(taskId));
    }

    @Test
    void shouldNotCreateJobTwinWhenJobTwinClassIdIsNull_Async() throws Exception {
        // given
        UUID taskId = UuidUtils.generate();
        TwinTriggerEntity triggerEntity = createTriggerEntityWithoutJobClass();

        TwinTrigger mockTrigger = mock(TwinTrigger.class);
        Properties triggerProps = new Properties();

        lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any())).thenReturn(triggerProps);
        lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class))).thenReturn(mockTrigger);

        // when
        twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus, taskId);

        // then
        verify(twinService, never()).createTwin(any(TwinCreate.class));

        // Verify trigger was still executed, just without job twin (jobTwinId is null)
        verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), isNull());
    }

    @Test
    void shouldCreateJobTwinWithTaskId_Sync() throws Exception {
        // given
        UUID taskId = UuidUtils.generate();
        UUID triggerId = UuidUtils.generate();
        UUID jobTwinClassId = jobTwinClass.getId();
        TwinTriggerEntity triggerEntity = createTriggerEntityWithJobClass(jobTwinClassId);
        triggerEntity.setId(triggerId);

        TwinTriggerTaskEntity taskEntity = new TwinTriggerTaskEntity()
                .setId(taskId)
                .setTwinId(twinEntity.getId())
                .setTwinTriggerId(triggerId);

        TwinTrigger mockTrigger = mock(TwinTrigger.class);
        Properties triggerProps = new Properties();

        when(twinTriggerTaskService.addSyncTask(eq(twinEntity.getId()), eq(triggerId), eq(srcStatus.getId())))
                .thenReturn(taskEntity);
        when(twinService.createTwin(any(TwinCreate.class))).thenReturn(createTwinCreateResult(new TwinEntity()));
        lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any())).thenReturn(triggerProps);
        lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class))).thenReturn(mockTrigger);

        // when
        twinTriggerService.runTriggerSync(triggerEntity, twinEntity, srcStatus, dstStatus);

        // then
        verify(twinTriggerTaskService).addSyncTask(twinEntity.getId(), triggerId, srcStatus.getId());

        ArgumentCaptor<TwinCreate> captor = ArgumentCaptor.forClass(TwinCreate.class);
        verify(twinService).createTwin(captor.capture());

        TwinCreate capturedCreate = captor.getValue();
        assertEquals(taskId, capturedCreate.getTwinEntity().getId(), "Job twin ID should match task ID");
        assertEquals(jobTwinClassId, capturedCreate.getTwinEntity().getTwinClassId(), "Job twin class ID should match");

        // Verify trigger was executed with task ID from the created task
        verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), eq(taskId));
    }

    @Test
    void shouldCreateTaskAndExecuteTriggerWithoutJobTwin_Sync() throws Exception {
        // given
        UUID taskId = UuidUtils.generate();
        UUID triggerId = UuidUtils.generate();
        TwinTriggerEntity triggerEntity = createTriggerEntityWithoutJobClass();
        triggerEntity.setId(triggerId);

        TwinTriggerTaskEntity taskEntity = new TwinTriggerTaskEntity()
                .setId(taskId)
                .setTwinId(twinEntity.getId())
                .setTwinTriggerId(triggerId);

        TwinTrigger mockTrigger = mock(TwinTrigger.class);
        Properties triggerProps = new Properties();

        when(twinTriggerTaskService.addSyncTask(eq(twinEntity.getId()), eq(triggerId), eq(srcStatus.getId())))
                .thenReturn(taskEntity);
        lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any())).thenReturn(triggerProps);
        lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class))).thenReturn(mockTrigger);

        // when
        twinTriggerService.runTriggerSync(triggerEntity, twinEntity, srcStatus, dstStatus);

        // then
        verify(twinTriggerTaskService).addSyncTask(twinEntity.getId(), triggerId, srcStatus.getId());
        verify(twinService, never()).createTwin(any(TwinCreate.class));

        // Verify trigger was executed with null jobTwinId (no job twin was created)
        verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), isNull());
    }
}
