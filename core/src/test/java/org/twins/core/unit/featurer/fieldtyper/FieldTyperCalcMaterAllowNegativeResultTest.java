package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link FieldTyperCalcMater#checkCalculatedResult} gate: by default ({@code allowNegativeResult=false},
 * also what an absent param resolves to) negative calculated results are rejected with
 * TWIN_CLASS_FIELD_VALUE_INCORRECT; fields that legitimately can go negative set the param to true.
 */
class FieldTyperCalcMaterAllowNegativeResultTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("remainingQuantityTask");
    }

    private Properties props(String allowNegativeResult) {
        var props = new Properties();
        if (allowNegativeResult != null)
            props.put(FieldTyperCalcMater.allowNegativeResult.getKey(), allowNegativeResult);
        return props;
    }

    @Test
    void negativeRejectedWhenParamAbsent() {
        // legacy configs carry no allowNegativeResult in hstore — the default (false) protects them
        var e = assertThrows(ServiceException.class,
                () -> new FieldTyperCalcMater() {}.checkCalculatedResult(field, new BigDecimal("-10.50"), props(null)));
        assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), e.getErrorCode());
    }

    @Test
    void negativeRejectedWhenExplicitlyFalse() {
        var e = assertThrows(ServiceException.class,
                () -> new FieldTyperCalcMater() {}.checkCalculatedResult(field, new BigDecimal("-0.01"), props("false")));
        assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), e.getErrorCode());
    }

    @Test
    void negativePassesWhenAllowed() throws ServiceException {
        var ret = new FieldTyperCalcMater() {}.checkCalculatedResult(field, new BigDecimal("-0.01"), props("true"));
        assertEquals(new BigDecimal("-0.01"), ret);
    }

    @Test
    void zeroAndPositivePassByDefault() throws ServiceException {
        var gate = new FieldTyperCalcMater() {};
        assertEquals(BigDecimal.ZERO, gate.checkCalculatedResult(field, BigDecimal.ZERO, props(null)));
        assertEquals(new BigDecimal("7.77"), gate.checkCalculatedResult(field, new BigDecimal("7.77"), props("false")));
    }

    @Test
    void nullResultPassesByDefault() throws ServiceException {
        var ret = new FieldTyperCalcMater() {}.checkCalculatedResult(field, null, props(null));
        assertEquals(null, ret);
    }
}
