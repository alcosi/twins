package org.twins.core.unit.domain.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TwinGroupLevelTest {
    @Test
    void selectValueUsesEachOptionOnceAndAnEmptyValueIsTheTypedNullKey() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        TwinGroupLevel level = level(field);
        UUID option = UUID.randomUUID();
        UUID otherOption = UUID.randomUUID();

        Collection<FieldValueSingle> keys = level.extractKeys(new FieldValueSelect(field).setItems(List.of(
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(otherOption))));
        assertEquals(List.of(option, otherOption), scalars(keys));
        FieldValueSelectSingle keyValue = assertInstanceOf(FieldValueSelectSingle.class, keys.iterator().next());
        // the option rides in the key value: no second load is needed downstream
        assertEquals(option, keyValue.getValue().getId());

        // an empty select value groups under the typed empty key
        Collection<FieldValueSingle> empty = level.extractKeys(new FieldValueSelect(field));
        assertEquals(Collections.singletonList(null), scalars(empty));
        assertInstanceOf(FieldValueSelectSingle.class, single(empty));
    }

    @Test
    void decimalAndTextValuesKeepTheirFormattedStringKey() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        // a decimal deserializes to FieldValueText; the formatted string is the group key
        assertEquals(List.of("0.00"), scalars(level(field).extractKeys(new FieldValueText(field).setValue("0.00"))));
    }

    @Test
    void dateValueIsUsedAsTheKey() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 0, 0);
        assertEquals(List.of(date), scalars(level(field).extractKeys(new FieldValueDate(field, null).setDate(date))));
    }

    @Test
    void linkValueUsesEachDstOnceAndAnEmptyValueIsTheTypedNullKey() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        TwinGroupLevel level = level(field);
        UUID dst = UUID.randomUUID();

        Collection<FieldValueSingle> keys = level.extractKeys(new FieldValueLink(field)
                .setItems(List.of(new TwinEntity().setId(dst), new TwinEntity().setId(dst))));
        assertEquals(List.of(dst), scalars(keys));
        // the far twin rides in the key value: no second load is needed downstream
        FieldValueLinkSingle keyValue = assertInstanceOf(FieldValueLinkSingle.class, single(keys));
        assertEquals(dst, keyValue.getValue().getId());

        Collection<FieldValueSingle> undefined = level.extractKeys(new FieldValueLink(field).undefine());
        assertEquals(Collections.singletonList(null), scalars(undefined));
        assertInstanceOf(FieldValueLinkSingle.class, single(undefined));
    }

    @Test
    void typedEmptyValueIsTheNullKeyOfItsType() throws Exception {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        // an undefined value handed over by TwinFieldService.getFieldValue keeps the level's key type
        Collection<FieldValueSingle> empty = level(field).extractKeys(new FieldValueUserSingle(field));
        assertEquals(Collections.singletonList(null), scalars(empty));
        assertInstanceOf(FieldValueUserSingle.class, single(empty));
    }

    @Test
    void collectionWithoutAScalarKeyIsRejected() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        ServiceException exception = assertThrows(ServiceException.class,
                () -> level(field).extractKeys(new FieldValueUser(field).setItems(List.of(new UserEntity().setId(UUID.randomUUID())))));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), exception.getErrorCode());
    }

    private static TwinGroupLevel level(TwinClassFieldEntity field) {
        return new TwinGroupLevel()
                .setFieldId(field.getId())
                .setField(field);
    }

    private static List<Object> scalars(Collection<FieldValueSingle> values) {
        List<Object> scalars = new ArrayList<>();
        for (FieldValueSingle value : values) {
            scalars.add(value.getScalarValue());
        }
        return scalars;
    }

    private static FieldValueSingle single(Collection<FieldValueSingle> values) {
        assertEquals(1, values.size());
        return values.iterator().next();
    }
}
