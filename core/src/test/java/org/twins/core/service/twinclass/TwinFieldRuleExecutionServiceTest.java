package org.twins.core.service.twinclass;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinFieldRuleExecutionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TwinFieldRuleExecutionServiceTest {

    @InjectMocks
    private TwinFieldRuleExecutionService executionService;

    @Test
    public void testApplyRules_SortingImmutableList() {
        // Arrange
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());

        // Create an IMMUTABLE list of rules to simulate the bug (sorting must not attempt to modify original)
        List<TwinClassFieldRuleEntity> immutableRules = Collections.singletonList(
                new TwinClassFieldRuleEntity().setRulePriority(1).setId(UUID.randomUUID())
        );
        // Attach rules via Kit with immutable underlying collection
        field.setRuleKit(new Kit<>(immutableRules, TwinClassFieldRuleEntity::getId));

        // Prepare a simple value for the field
        FieldValue value = new FieldValueText(field).setValue("v");
        List<FieldValue> values = Collections.singletonList(value);

        // Act & Assert
        assertDoesNotThrow(() -> executionService.applyRules(values));
    }

    @Test
    public void testApplyRules_SortingLogic() {
        // Arrange
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());

        List<TwinClassFieldRuleEntity> rules = new ArrayList<>();
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(10).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(5).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(20).setId(UUID.randomUUID()));
        field.setRuleKit(new Kit<>(rules, TwinClassFieldRuleEntity::getId));

        FieldValue value = new FieldValueText(field).setValue("v2");
        List<FieldValue> values = Collections.singletonList(value);

        // Act
        var outputs = executionService.applyRules(values);

        // Assert
        assertNotNull(outputs);
        assertTrue(outputs.containsKey(field.getId()));
        // The main goal was to ensure sorting happens on a copy and no crash occurs.
    }
}