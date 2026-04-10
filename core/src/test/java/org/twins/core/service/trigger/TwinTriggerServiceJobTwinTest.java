package org.twins.core.service.trigger;

import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.twins.core.dao.trigger.TwinTriggerRepository;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class TwinTriggerServiceJobTwinTest {

    @Mock
    private TwinTriggerRepository repository;

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassService twinClassService;

    @InjectMocks
    private TwinTriggerService twinTriggerService;

    private TwinTriggerEntity triggerEntity;
    private TwinEntity twinEntity;
    private TwinStatusEntity srcStatus;
    private TwinStatusEntity dstStatus;

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

        triggerEntity = new TwinTriggerEntity()
                .setId(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setTwinTriggerFeaturerId(1503)
                .setTwinTriggerParam(new HashMap<>());
    }

    @Test
    void shouldCreateJobTwinWithTwinClassJobSimple() throws Exception {
        // given
        UUID jobTwinClassId = SystemEntityService.TWIN_CLASS_JOB_SIMPLE;
        triggerEntity.setJobTwinClassId(jobTwinClassId);

        TwinClassEntity jobTwinClass = new TwinClassEntity()
                .setId(jobTwinClassId)
                .setKey("JOB_SIMPLE");

        TwinEntity createdJobTwin = new TwinEntity()
                .setId(triggerEntity.getId())
                .setTwinClassId(jobTwinClassId)
                .setTwinClass(jobTwinClass);

        TwinCreateResult createResult = new TwinCreateResult();
        createResult.setCreatedTwin(createdJobTwin);

        when(twinClassService.findEntitySafe(jobTwinClassId)).thenReturn(jobTwinClass);
        when(twinService.createTwin(any(TwinCreate.class))).thenReturn(createResult);
        lenient().when(featurerService.extractProperties(eq(1503), any())).thenReturn(new Properties());
        lenient().when(featurerService.getFeaturer(eq(1503), eq(TwinTrigger.class))).thenReturn(mock(TwinTrigger.class));

        // when
        twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus);

        // then
        ArgumentCaptor<TwinCreate> captor = ArgumentCaptor.forClass(TwinCreate.class);
        verify(twinService).createTwin(captor.capture());

        TwinCreate capturedCreate = captor.getValue();
        assertEquals(triggerEntity.getId(), capturedCreate.getTwinEntity().getId());
        assertEquals(SystemEntityService.TWIN_CLASS_JOB_SIMPLE, capturedCreate.getTwinEntity().getTwinClassId());
        assertEquals(org.twins.core.domain.twinoperation.TwinOperation.Launcher.trigger, capturedCreate.getLauncher());
        assertFalse(capturedCreate.isCanTriggerAfterOperationFactory());
    }

    @Test
    void shouldNotCreateJobTwinWhenJobTwinClassIdIsNull() throws Exception {
        // given
        triggerEntity.setJobTwinClassId(null);

        lenient().when(featurerService.extractProperties(eq(1503), any())).thenReturn(new Properties());
        lenient().when(featurerService.getFeaturer(eq(1503), eq(TwinTrigger.class))).thenReturn(mock(TwinTrigger.class));

        // when
        twinTriggerService.runTrigger(triggerEntity, twinEntity, srcStatus, dstStatus);

        // then
        verify(twinClassService, never()).findEntitySafe(any(UUID.class));
        verify(twinService, never()).createTwin(any(TwinCreate.class));
    }
}
