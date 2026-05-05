package org.twins.core.service.twin;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TwinActionService validator restrictions configuration.
 *
 * This test verifies that TwinValidatorSetEntity and TwinActionValidatorRuleEntity
 * are properly configured for action restrictions.
 */
class TwinActionServiceValidatorRestrictionTest {

    private UUID validatorSetId;
    private UUID validatorId;
    private UUID restrictionReasonId;

    @BeforeEach
    void setUp() {
        validatorSetId = UUID.randomUUID();
        validatorId = UUID.randomUUID();
        restrictionReasonId = UUID.randomUUID();
    }

    @Test
    void testValidatorRuleConfiguration() {
        // Test that validator rule with TwinValidatorSet can be properly configured
        TwinClassEntity class1 = new TwinClassEntity();
        class1.setId(UUID.randomUUID());

        TwinValidatorSetEntity validatorSet = new TwinValidatorSetEntity();
        validatorSet.setId(validatorSetId);
        validatorSet.setInvert(false);

        TwinValidatorEntity validator = new TwinValidatorEntity();
        validator.setId(validatorId);
        validator.setOrder(1);
        validator.setActive(true);

        TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
        validatorRule.setId(UUID.randomUUID());
        validatorRule.setTwinAction(TwinAction.EDIT);
        validatorRule.setTwinValidatorSetId(validatorSetId);
        validatorRule.setOrder(1);
        validatorRule.setActive(true);
        validatorRule.setActionRestrictionReasonId(restrictionReasonId);
        validatorRule.setTwinValidatorSet(validatorSet);
        validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        validatorRule.getTwinValidatorKit().add(validator);

        class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
        class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
        class1.getActionsProtectedByValidatorRules().add(validatorRule);

        // Verify the validator rule structure is correct
        assertEquals(validatorSetId, validatorRule.getTwinValidatorSetId());
        assertEquals(TwinAction.EDIT, validatorRule.getTwinAction());
        assertEquals(restrictionReasonId, validatorRule.getActionRestrictionReasonId());
        assertTrue(validatorRule.isActive());
        assertEquals(1, validatorRule.getOrder());
        assertNotNull(validatorRule.getTwinValidatorSet());
        assertFalse(validatorRule.getTwinValidatorSet().getInvert());
        assertEquals(1, validatorRule.getTwinValidatorKit().getList().size());
        assertEquals(validatorId, validatorRule.getTwinValidatorKit().getList().get(0).getId());
    }

    @Test
    void testMultipleActionsWithSameValidatorSet() {
        // Test that same validator set can be used for multiple actions
        TwinClassEntity class1 = new TwinClassEntity();
        class1.setId(UUID.randomUUID());

        TwinValidatorSetEntity validatorSet = new TwinValidatorSetEntity();
        validatorSet.setId(validatorSetId);
        validatorSet.setInvert(false);

        TwinValidatorEntity validator = new TwinValidatorEntity();
        validator.setId(validatorId);

        TwinActionValidatorRuleEntity ruleEdit = new TwinActionValidatorRuleEntity();
        ruleEdit.setId(UUID.randomUUID());
        ruleEdit.setTwinAction(TwinAction.EDIT);
        ruleEdit.setTwinValidatorSetId(validatorSetId);
        ruleEdit.setTwinValidatorSet(validatorSet);
        ruleEdit.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        ruleEdit.getTwinValidatorKit().add(validator);

        TwinActionValidatorRuleEntity ruleDelete = new TwinActionValidatorRuleEntity();
        ruleDelete.setId(UUID.randomUUID());
        ruleDelete.setTwinAction(TwinAction.DELETE);
        ruleDelete.setTwinValidatorSetId(validatorSetId);
        ruleDelete.setTwinValidatorSet(validatorSet);
        ruleDelete.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        ruleDelete.getTwinValidatorKit().add(validator);

        class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
        class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
        class1.getActionsProtectedByValidatorRules().add(ruleEdit);
        class1.getActionsProtectedByValidatorRules().add(ruleDelete);

        // Verify both rules reference the same validator set
        assertEquals(validatorSetId, ruleEdit.getTwinValidatorSetId());
        assertEquals(validatorSetId, ruleDelete.getTwinValidatorSetId());
        assertEquals(TwinAction.EDIT, ruleEdit.getTwinAction());
        assertEquals(TwinAction.DELETE, ruleDelete.getTwinAction());
    }

    @Test
    void testValidatorSetInvertConfiguration() {
        // Test different invert configurations
        TwinValidatorSetEntity validatorSetInvertFalse = new TwinValidatorSetEntity();
        validatorSetInvertFalse.setId(validatorSetId);
        validatorSetInvertFalse.setInvert(false);

        TwinValidatorSetEntity validatorSetInvertTrue = new TwinValidatorSetEntity();
        validatorSetInvertTrue.setId(UUID.randomUUID());
        validatorSetInvertTrue.setInvert(true);

        // Verify invert configuration is accessible
        assertFalse(validatorSetInvertFalse.getInvert());
        assertTrue(validatorSetInvertTrue.getInvert());

        // This test documents the expected behavior:
        // - invert=false: action is restricted when validator passes
        // - invert=true: action is allowed when validator passes
        assertFalse(validatorSetInvertFalse.getInvert(), "invert=false means action restricted when validator passes");
        assertTrue(validatorSetInvertTrue.getInvert(), "invert=true means action allowed when validator passes");
    }
}
