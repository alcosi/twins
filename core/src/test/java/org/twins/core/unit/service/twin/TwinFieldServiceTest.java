package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.twin.TwinFieldService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit contract of TwinFieldService — the field access behind twin grouping. checkGroupable is the
 * pre-search gate (base fields need a scalar column, dynamic fields an accepted storage);
 * getFieldValue is the single read point: base fields come from twin columns (an attached entity
 * wins over the bare id, an id-only key becomes a reference for bulk materialization), dynamic
 * fields come from twin.fieldValuesKit, and an absent value comes back as an undefined value of
 * the field's type, never null.
 */
class TwinFieldServiceTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private FeaturerService featurerService;

    @InjectMocks
    private TwinFieldService twinFieldService;

    @Test
    void bareBaseFieldIdBecomesAnIdOnlyReference() throws Exception {
        UUID assigneeId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(assigneeId);

        FieldValueReferenceSingle value = assertInstanceOf(FieldValueReferenceSingle.class, twinFieldService.getFieldValue(twin, field));
        assertEquals(FieldValueUserSingle.class, value.getValueType());
        assertEquals(assigneeId, value.getId());
        // the id itself is the scalar key
        assertEquals(assigneeId, value.getScalarValue());
    }

    @Test
    void entityAttachedToTheTwinBecomesTheConcreteValueAtOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity assigner = new UserEntity().setId(userId).setName("Ann");
        TwinEntity twin = new TwinEntity()
                .setId(UUID.randomUUID())
                .setAssignerUserId(userId)
                .setAssignerUser(assigner);

        // the concrete value arrives directly: no reference in between, nothing to materialize
        FieldValueUserSingle value = assertInstanceOf(FieldValueUserSingle.class,
                twinFieldService.getFieldValue(twin, new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)));
        assertEquals(assigner, value.getValue());

        // an EAGER association works the same way as a filled transient
        TwinStatusEntity status = new TwinStatusEntity().setId(UUID.randomUUID());
        twin.setTwinStatus(status);
        FieldValueStatus statusValue = assertInstanceOf(FieldValueStatus.class,
                twinFieldService.getFieldValue(twin, new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.STATUS_ID)));
        assertEquals(status, statusValue.getValue());
    }

    @Test
    void missingBaseKeyComesBackAsTypedEmpty() throws Exception {
        FieldValueUserSingle value = assertInstanceOf(FieldValueUserSingle.class, twinFieldService.getFieldValue(
                new TwinEntity().setId(UUID.randomUUID()),
                new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)));
        assertNull(value.getValue());
        assertNull(value.getScalarValue());
    }

    @Test
    void flavorHasNoRelationAndStaysAReference() throws Exception {
        UUID optionId = UUID.randomUUID();
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID()).setFlavorDataListOptionId(optionId);

        FieldValueReferenceSingle value = assertInstanceOf(FieldValueReferenceSingle.class,
                twinFieldService.getFieldValue(twin, new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.FLAVOR_ID)));
        assertEquals(FieldValueSelectSingle.class, value.getValueType());
        assertEquals(optionId, value.getId());
    }

    @Test
    void systemFieldWithoutAScalarColumnIsRejected() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.NAME);
        ServiceException exception = assertThrows(ServiceException.class,
                () -> twinFieldService.getFieldValue(new TwinEntity().setId(UUID.randomUUID()), field));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void groupableBaseFieldsPassTheCheck() throws Exception {
        assertDoesNotThrow(() -> twinFieldService.checkGroupable(
                new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)));
        assertDoesNotThrow(() -> twinFieldService.checkGroupable(
                new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.STATUS_ID)));
    }

    @Test
    void tagsAndMarkersAreGroupableKitFields() {
        assertDoesNotThrow(() -> twinFieldService.checkGroupable(
                new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.TAGS)));
        assertDoesNotThrow(() -> twinFieldService.checkGroupable(
                new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.MARKERS)));
    }

    @Test
    void aliasSystemFieldIsNotGroupable() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ALIASES);
        ServiceException exception = assertThrows(ServiceException.class, () -> twinFieldService.checkGroupable(field));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void kitBackedSystemFieldsDeserializeThroughTheTyper() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.TAGS);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        TwinField twinField = mock(TwinField.class);
        FieldValueSelect select = new FieldValueSelect(field);
        when(twinService.wrapField(twin, field)).thenReturn(twinField);
        when(twinService.deserializeValue(twinField)).thenReturn(select);

        // the tag options come from the typer (twinTagKit -> FieldValueSelect), not from the kit
        assertEquals(select, twinFieldService.getFieldValue(twin, field));
    }

    @Test
    void baseFieldValueIsCachedOnTheTwin() throws Exception {
        UUID assigneeId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(assigneeId);

        FieldValue first = twinFieldService.getFieldValue(twin, field);

        assertSame(first, twinFieldService.getFieldValue(twin, field));
        assertSame(first, twin.getBaseFieldValuesKit().get(field.getId()));
    }

    @Test
    void kitBackedValueIsCachedAndDeserializesOnlyOnce() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.TAGS);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        TwinField twinField = mock(TwinField.class);
        FieldValueSelect select = new FieldValueSelect(field);
        when(twinService.wrapField(twin, field)).thenReturn(twinField);
        when(twinService.deserializeValue(twinField)).thenReturn(select);

        assertSame(select, twinFieldService.getFieldValue(twin, field));
        assertSame(select, twinFieldService.getFieldValue(twin, field));
        verify(twinService, times(1)).deserializeValue(any(TwinField.class));
    }

    @Test
    void dynamicFieldWithAcceptedStorageIsGroupable() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID()).setFieldTyperFeaturerId(1310);
        FieldTyper typer = mock(FieldTyper.class);
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorageSimple.class));

        assertDoesNotThrow(() -> twinFieldService.checkGroupable(field));
    }

    @Test
    void dynamicFieldWithUnsupportedStorageIsNotGroupable() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID()).setFieldTyperFeaturerId(1310);
        FieldTyper typer = mock(FieldTyper.class);
        when(featurerService.getFeaturer(1310, FieldTyper.class)).thenReturn(typer);
        // a bare storage mock is none of the six accepted types
        when(typer.getStorage(field)).thenReturn(mock(TwinFieldStorage.class));

        ServiceException exception = assertThrows(ServiceException.class, () -> twinFieldService.checkGroupable(field));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    @Test
    void dynamicFieldReadsTheKit() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        FieldValueText text = new FieldValueText(field).setValue("some value");
        twin.getFieldValuesKit().add(text);

        assertEquals(text, twinFieldService.getFieldValue(twin, field));
    }

    @Test
    void absentDynamicValueComesBackAsTypedEmpty() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        when(twinService.createFieldValue(any(TwinClassFieldEntity.class))).thenReturn(new FieldValueText(field));

        FieldValueText value = assertInstanceOf(FieldValueText.class,
                twinFieldService.getFieldValue(new TwinEntity().setId(UUID.randomUUID()), field));
        assertNull(value.getScalarValue());
    }
}
