package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinFieldsFilledBySearchId;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinFieldRuleExecutionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinFieldsFilledBySearchIdTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldSearchService twinClassFieldSearchService;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinFieldRuleExecutionService twinFieldRuleExecutionService;

    private ConditionerFactoryItemTwinFieldsFilledBySearchId conditioner;

    @BeforeEach
    void setUp() {
        // @RequiredArgsConstructor: (twinClassFieldSearchService, twinService, twinFieldRuleExecutionService)
        conditioner = new ConditionerFactoryItemTwinFieldsFilledBySearchId(
                twinClassFieldSearchService, twinService, twinFieldRuleExecutionService);
    }

    private Properties props(UUID searchId) {
        var p = new Properties();
        p.put("searchId", searchId.toString());
        return p;
    }

    private FactoryItem item(TwinEntity twin) {
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id);
    }

    private void stubSearchReturns(List<TwinClassFieldEntity> fields) throws ServiceException {
        when(twinClassFieldSearchService.findTwinClassField(
                any(UUID.class), any(Map.class), eq(null), eq(SimplePagination.ALL)))
                .thenReturn(new PaginationResult<TwinClassFieldEntity>().setList(fields).setTotal(fields.size()));
    }

    @Nested
    class Check {

        @Test
        void check_noRequiredFields_returnsTrue() throws ServiceException {
            // contract: nothing required -> trivially filled.
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of());

            assertTrue(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_allRequiredFieldsFilled_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of(field(fieldId)));

            var fv = mock(FieldValue.class);
            when(fv.isEmpty()).thenReturn(false);
            when(fv.getTwinClassFieldId()).thenReturn(fieldId);
            var kit = new Kit<FieldValue, UUID>(FieldValue::getTwinClassFieldId);
            kit.add(fv);
            twin.setFieldValuesKit(kit);
            when(twinFieldRuleExecutionService.isRequired(eq(twin), any(TwinClassFieldEntity.class)))
                    .thenReturn(true);

            assertTrue(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_requiredFieldValueMissing_returnsFalse() throws ServiceException {
            // contract: a required field whose value is absent from the kit -> not filled.
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of(field(fieldId)));

            // kit non-empty (a DIFFERENT field present) so the empty-kit guard is passed, but the
            // required field is absent -> fieldValuesKit.get(fieldId) == null, and isRequired IS consulted
            var otherFv = mock(FieldValue.class);
            when(otherFv.getTwinClassFieldId()).thenReturn(UUID.randomUUID());
            var kit = new Kit<FieldValue, UUID>(FieldValue::getTwinClassFieldId);
            kit.add(otherFv);
            twin.setFieldValuesKit(kit);
            when(twinFieldRuleExecutionService.isRequired(eq(twin), any(TwinClassFieldEntity.class)))
                    .thenReturn(true);

            assertFalse(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_requiredFieldValueEmpty_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of(field(fieldId)));

            var fv = mock(FieldValue.class);
            when(fv.isEmpty()).thenReturn(true);
            when(fv.getTwinClassFieldId()).thenReturn(fieldId);
            var kit = new Kit<FieldValue, UUID>(FieldValue::getTwinClassFieldId);
            kit.add(fv);
            twin.setFieldValuesKit(kit);
            when(twinFieldRuleExecutionService.isRequired(eq(twin), any(TwinClassFieldEntity.class)))
                    .thenReturn(true);

            assertFalse(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_fieldNotRequiredAndNoExternalFlag_returnsTrue() throws ServiceException {
            // contract: a field that is neither rule-required nor requiredOnAnyMarketplace is skipped.
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of(field(UUID.randomUUID())));
            // kit non-empty (passes the empty-kit guard) so the not-required field is skipped -> true.
            // Real FieldValue (not a mock) -> no stub needed for kit keying.
            var presentFv = new FieldValueText(new TwinClassFieldEntity().setId(UUID.randomUUID()));
            var kit = new Kit<FieldValue, UUID>(FieldValue::getTwinClassFieldId);
            kit.add(presentFv);
            twin.setFieldValuesKit(kit);
            when(twinFieldRuleExecutionService.isRequired(eq(twin), any(TwinClassFieldEntity.class)))
                    .thenReturn(false);

            assertTrue(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_requiredOnAnyMarketplaceExternalProp_treatedAsRequired() throws ServiceException {
            // contract: externalProperties.requiredOnAnyMarketplace == "true" forces the required check
            // even when the rule engine says isRequired == false.
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            var f = field(fieldId);
            Map<String, String> ext = new HashMap<>();
            ext.put("requiredOnAnyMarketplace", "true");
            f.setExternalProperties(ext);
            stubSearchReturns(List.of(f));

            var fv = mock(FieldValue.class);
            when(fv.isEmpty()).thenReturn(true); // empty -> not filled
            when(fv.getTwinClassFieldId()).thenReturn(fieldId);
            var kit = new Kit<FieldValue, UUID>(FieldValue::getTwinClassFieldId);
            kit.add(fv);
            twin.setFieldValuesKit(kit);
            when(twinFieldRuleExecutionService.isRequired(eq(twin), any(TwinClassFieldEntity.class)))
                    .thenReturn(false);

            assertFalse(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }

        @Test
        void check_fieldValuesKitEmpty_returnsFalse() throws ServiceException {
            // contract: required fields exist but no field values loaded at all -> log warn + false.
            var twin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            stubSearchReturns(List.of(field(UUID.randomUUID())));
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId)); // empty Kit -> isEmpty true

            assertFalse(conditioner.check(props(UUID.randomUUID()), item(twin)));
        }
    }
}
