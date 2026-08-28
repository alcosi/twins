package org.twins.core.unit.service.twin;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinActionServiceValidatorRestrictionTest extends BaseUnitTest {

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
    void validatorRule_withValidatorSet_configurableCorrectly() {
        var twinClass = new TwinClassEntity();
        twinClass.setId(UUID.randomUUID());

        var validatorSet = new TwinValidatorSetEntity();
        validatorSet.setId(validatorSetId);
        validatorSet.setInvert(false);

        var validator = new TwinValidatorEntity();
        validator.setId(validatorId);
        validator.setOrder(1);
        validator.setActive(true);

        var validatorRule = new TwinActionValidatorRuleEntity();
        validatorRule.setId(UUID.randomUUID());
        validatorRule.setTwinAction(TwinAction.EDIT);
        validatorRule.setTwinValidatorSetId(validatorSetId);
        validatorRule.setOrder(1);
        validatorRule.setActive(true);
        validatorRule.setActionRestrictionReasonId(restrictionReasonId);
        validatorRule.setTwinValidatorSet(validatorSet);
        validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        validatorRule.getTwinValidatorKit().add(validator);

        twinClass.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
        twinClass.setActionsProtectedByValidatorRules(
                new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
        twinClass.getActionsProtectedByValidatorRules().add(validatorRule);

        assertEquals(validatorSetId, validatorRule.getTwinValidatorSetId());
        assertEquals(TwinAction.EDIT, validatorRule.getTwinAction());
        assertEquals(restrictionReasonId, validatorRule.getActionRestrictionReasonId());
        assertTrue(validatorRule.isActive());
        assertEquals(1, validatorRule.getOrder());
        assertFalse(validatorRule.getTwinValidatorSet().getInvert());
        assertEquals(1, validatorRule.getTwinValidatorKit().getList().size());
        assertEquals(validatorId, validatorRule.getTwinValidatorKit().getList().get(0).getId());
    }

    @Test
    void validatorRule_sameValidatorSet_reusableAcrossActions() {
        var twinClass = new TwinClassEntity();
        twinClass.setId(UUID.randomUUID());

        var validatorSet = new TwinValidatorSetEntity();
        validatorSet.setId(validatorSetId);
        validatorSet.setInvert(false);

        var validator = new TwinValidatorEntity();
        validator.setId(validatorId);

        var ruleEdit = new TwinActionValidatorRuleEntity();
        ruleEdit.setId(UUID.randomUUID());
        ruleEdit.setTwinAction(TwinAction.EDIT);
        ruleEdit.setTwinValidatorSetId(validatorSetId);
        ruleEdit.setTwinValidatorSet(validatorSet);
        ruleEdit.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        ruleEdit.getTwinValidatorKit().add(validator);

        var ruleDelete = new TwinActionValidatorRuleEntity();
        ruleDelete.setId(UUID.randomUUID());
        ruleDelete.setTwinAction(TwinAction.DELETE);
        ruleDelete.setTwinValidatorSetId(validatorSetId);
        ruleDelete.setTwinValidatorSet(validatorSet);
        ruleDelete.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        ruleDelete.getTwinValidatorKit().add(validator);

        twinClass.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
        twinClass.setActionsProtectedByValidatorRules(
                new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
        twinClass.getActionsProtectedByValidatorRules().add(ruleEdit);
        twinClass.getActionsProtectedByValidatorRules().add(ruleDelete);

        assertEquals(validatorSetId, ruleEdit.getTwinValidatorSetId());
        assertEquals(validatorSetId, ruleDelete.getTwinValidatorSetId());
        assertEquals(TwinAction.EDIT, ruleEdit.getTwinAction());
        assertEquals(TwinAction.DELETE, ruleDelete.getTwinAction());
    }

    @Test
    void validatorSet_invertFlag_accessibleAndDistinct() {
        var invertFalse = new TwinValidatorSetEntity();
        invertFalse.setId(validatorSetId);
        invertFalse.setInvert(false);

        var invertTrue = new TwinValidatorSetEntity();
        invertTrue.setId(UUID.randomUUID());
        invertTrue.setInvert(true);

        assertFalse(invertFalse.getInvert());
        assertTrue(invertTrue.getInvert());
    }
}
