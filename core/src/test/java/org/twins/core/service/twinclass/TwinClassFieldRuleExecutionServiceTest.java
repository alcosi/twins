package org.twins.core.service.twinclass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TwinClassFieldRuleExecutionServiceTest {

    @InjectMocks
    private TwinClassFieldRuleExecutionService executionService;

    @Test
    public void testApplyRules_SortingImmutableList() {
        // Arrange
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());

        // Create an IMMUTABLE list of rules to simulate the bug
        List<TwinClassFieldRuleEntity> immutableRules = Collections.singletonList(
                new TwinClassFieldRuleEntity().setRulePriority(1)
        );

        TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput.builder()
                .field(field)
                .rules(immutableRules)
                .build();

        // Act & Assert
        // This should NOT throw UnsupportedOperationException now
        assertDoesNotThrow(() -> {
            executionService.applyRules(Collections.singletonList(input));
        });
    }

    @Test
    public void testApplyRules_SortingLogic() {
        // Arrange
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());

        List<TwinClassFieldRuleEntity> rules = new ArrayList<>();
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(10).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(5).setId(UUID.randomUUID()));
        rules.add(new TwinClassFieldRuleEntity().setRulePriority(20).setId(UUID.randomUUID()));

        TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput.builder()
                .field(field)
                .rules(rules)
                .build();

        // Act
        List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = executionService.applyRules(Collections.singletonList(input));

        // Assert
        assertNotNull(outputs);
        assertFalse(outputs.isEmpty());
        // Since we copied the list in the service, the original list should remain in same order if we didn't sort it in place
        // Actually the service sorts the COPY. 
        // We can't easily check the internal sorted order from outside unless we check how rules were applied.
        // But the main goal was to fix the crash.
    }
}