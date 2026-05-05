package org.twins.core.unit.service.trigger;

import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.trigger.TwinTrigger;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.trigger.TwinTriggerService;
import org.twins.core.service.trigger.TwinTriggerTaskService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinService.TwinCreateResult;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TwinTriggerServiceJobTwinTest extends BaseUnitTest {

    private static final int TEST_TRIGGER_FEATURER_ID = 1503;

    @Mock private FeaturerService featurerService;
    @Mock private TwinService twinService;
    @Mock private TwinClassService twinClassService;
    @Mock private TwinTriggerTaskService twinTriggerTaskService;
    @Mock private TwinTrigger mockTrigger;

    @InjectMocks private TwinTriggerService twinTriggerService;

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

        var jobTwinClassId = UuidUtils.generate();
        jobTwinClass = new TwinClassEntity()
                .setId(jobTwinClassId)
                .setKey("JOB");

        lenient().when(twinClassService.findEntitySafe(jobTwinClassId)).thenReturn(jobTwinClass);
    }

    @Nested
    class Async {

        @Test
        void runTrigger_withJobTwinClass_createsJobTwinWithTaskId() throws Exception {
            var taskId = UuidUtils.generate();
            var jobTwinClassId = jobTwinClass.getId();
            var triggerEntity = triggerWithJobClass(jobTwinClassId);

            lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any()))
                    .thenReturn(new Properties());
            lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class)))
                    .thenReturn(mockTrigger);
            when(twinService.createTwin(any(TwinCreate.class)))
                    .thenReturn(new TwinCreateResult().setCreatedTwin(new TwinEntity()));

            twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus, taskId);

            var captor = ArgumentCaptor.forClass(TwinCreate.class);
            verify(twinService).createTwin(captor.capture());

            var captured = captor.getValue();
            assertEquals(taskId, captured.getTwinEntity().getId());
            assertEquals(jobTwinClassId, captured.getTwinEntity().getTwinClassId());
            assertFalse(captured.isCanTriggerAfterOperationFactory());
            verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), eq(taskId));
        }

        @Test
        void runTrigger_withoutJobTwinClass_skipsTwinCreation() throws Exception {
            var taskId = UuidUtils.generate();
            var triggerEntity = triggerWithoutJobClass();

            lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any()))
                    .thenReturn(new Properties());
            lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class)))
                    .thenReturn(mockTrigger);

            twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus, taskId);

            verify(twinService, never()).createTwin(any(TwinCreate.class));
            verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), isNull());
        }
    }

    @Nested
    class Sync {

        @Test
        void runTriggerSync_withJobTwinClass_createsJobTwinWithTaskId() throws Exception {
            var triggerId = UuidUtils.generate();
            var jobTwinClassId = jobTwinClass.getId();
            var triggerEntity = triggerWithJobClass(jobTwinClassId);
            triggerEntity.setId(triggerId);

            var taskId = UuidUtils.generate();
            var taskEntity = new TwinTriggerTaskEntity()
                    .setId(taskId)
                    .setTwinId(twinEntity.getId())
                    .setTwinTriggerId(triggerId);

            when(twinTriggerTaskService.addSyncTask(eq(twinEntity.getId()), eq(triggerId), eq(srcStatus.getId())))
                    .thenReturn(taskEntity);
            when(twinService.createTwin(any(TwinCreate.class)))
                    .thenReturn(new TwinCreateResult().setCreatedTwin(new TwinEntity()));
            lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any()))
                    .thenReturn(new Properties());
            lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class)))
                    .thenReturn(mockTrigger);

            twinTriggerService.runTriggerSync(triggerEntity, twinEntity, srcStatus, dstStatus);

            verify(twinTriggerTaskService).addSyncTask(twinEntity.getId(), triggerId, srcStatus.getId());

            var captor = ArgumentCaptor.forClass(TwinCreate.class);
            verify(twinService).createTwin(captor.capture());
            assertEquals(taskId, captor.getValue().getTwinEntity().getId());
            assertEquals(jobTwinClassId, captor.getValue().getTwinEntity().getTwinClassId());
            verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), eq(taskId));
        }

        @Test
        void runTriggerSync_withoutJobTwinClass_skipsTwinCreation() throws Exception {
            var triggerId = UuidUtils.generate();
            var triggerEntity = triggerWithoutJobClass();
            triggerEntity.setId(triggerId);

            var taskId = UuidUtils.generate();
            var taskEntity = new TwinTriggerTaskEntity()
                    .setId(taskId)
                    .setTwinId(twinEntity.getId())
                    .setTwinTriggerId(triggerId);

            when(twinTriggerTaskService.addSyncTask(eq(twinEntity.getId()), eq(triggerId), eq(srcStatus.getId())))
                    .thenReturn(taskEntity);
            lenient().when(featurerService.extractProperties(eq(TEST_TRIGGER_FEATURER_ID), any()))
                    .thenReturn(new Properties());
            lenient().when(featurerService.getFeaturer(eq(TEST_TRIGGER_FEATURER_ID), eq(TwinTrigger.class)))
                    .thenReturn(mockTrigger);

            twinTriggerService.runTriggerSync(triggerEntity, twinEntity, srcStatus, dstStatus);

            verify(twinTriggerTaskService).addSyncTask(twinEntity.getId(), triggerId, srcStatus.getId());
            verify(twinService, never()).createTwin(any(TwinCreate.class));
            verify(mockTrigger).run(any(Properties.class), eq(twinEntity), eq(srcStatus), eq(dstStatus), isNull());
        }
    }

    private TwinTriggerEntity triggerWithJobClass(UUID jobTwinClassId) {
        return new TwinTriggerEntity()
                .setId(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setTwinTriggerFeaturerId(TEST_TRIGGER_FEATURER_ID)
                .setJobTwinClassId(jobTwinClassId)
                .setJobTwinClass(jobTwinClass)
                .setTwinTriggerParam(new HashMap<>());
    }

    private TwinTriggerEntity triggerWithoutJobClass() {
        return new TwinTriggerEntity()
                .setId(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setTwinTriggerFeaturerId(TEST_TRIGGER_FEATURER_ID)
                .setJobTwinClassId(null)
                .setJobTwinClass(null)
                .setTwinTriggerParam(new HashMap<>());
    }
}
