package org.twins.core.unit.service.twinclass;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinFieldRuleExecutionService;
import org.twins.core.service.twinclass.TwinClassFieldRuleMapService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

class TwinFieldRuleExecutionServiceTest extends BaseUnitTest {

    @Mock private TwinClassFieldRuleMapService twinClassFieldRuleMapService;
    @InjectMocks private TwinFieldRuleExecutionService executionService;

    @Test
    void applyRules_immutableRuleList_doesNotThrow() throws Exception {
        var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        var immutableRules = Collections.singletonList(
                new TwinClassFieldRuleEntity().setRulePriority(1).setId(UUID.randomUUID())
        );
        field.setRuleKit(new Kit<>(immutableRules, TwinClassFieldRuleEntity::getId));

        FieldValue value = new FieldValueText(field).setValue("v");
        doNothing().when(twinClassFieldRuleMapService).loadRules(anyList(), eq(true));

        assertDoesNotThrow(() -> executionService.applyRules(Collections.singletonList(value), new TwinEntity()));
    }

    @Test
    void applyRules_rulesOutOfOrder_outputsForAllFields() throws Exception {
        var field = new TwinClassFieldEntity().setId(UUID.randomUUID());

        var rules = new ArrayList<TwinClassFieldRuleEntity>();
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(10).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(5).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(20).setId(UUID.randomUUID()));
        field.setRuleKit(new Kit<>(rules, TwinClassFieldRuleEntity::getId));

        FieldValue value = new FieldValueText(field).setValue("v2");
        doNothing().when(twinClassFieldRuleMapService).loadRules(anyList(), eq(true));

        var outputs = executionService.applyRules(Collections.singletonList(value), new TwinEntity());

        assertNotNull(outputs);
        assertTrue(outputs.containsKey(field.getId()));
    }
}
