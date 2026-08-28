package org.twins.core.featurer.fieldtyper;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.history.HistoryItem;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_1317,
        name = "Decimal",
        description = "Decimal field with dedicated table storage"
)
public class FieldTyperDecimal extends FieldTyperSingleValue<
        FieldDescriptorNumeric,
        FieldValueText,
        TwinFieldDecimalEntity,
        BigDecimal,
        TwinFieldStorageDecimal,
        TwinFieldValueSearchNumeric> implements FieldTyperNumeric {

    @FeaturerParam(name = "Allow increment/decrement values (+/-)", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean allowIncrementValue = new FeaturerParamBoolean("allowIncrementValue");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties))
                .round(round.extract(properties))
                .extraThousandSeparators(extraThousandSeparatorSet.extract(properties))
                .extraDecimalSeparators(extraDecimalSeparatorSet.extract(properties));
    }

    @Override
    protected void setEntityValue(TwinFieldDecimalEntity twinFieldEntity, BigDecimal newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected BigDecimal getEntityValue(TwinFieldDecimalEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldDecimalEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldDecimalKit();
    }

    @Override
    protected TwinFieldDecimalEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldDecimalEntity.of(twin, twinClassField);
    }

    @Override
    protected BigDecimal processValue(Properties properties, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        var rawValue = value.getValue();
        if (isIncrementInput(properties, rawValue)) {
            return processIncrementedValue(properties, twinFieldEntity, new BigDecimal(rawValue));
        }
        return new BigDecimal(processAndFormatValue(properties, value));
    }

    /**
     * Folds a {@code +/-} delta into the field's current value in application memory and stores the
     * final value in {@link TwinFieldDecimalEntity}, as opposed to {@link FieldTyperDecimalIncrement},
     * which persists only the delta and lets the database apply it atomically
     * ({@code UPDATE ... SET value = value + ?}).
     * <p>
     * <b>Why non-atomic read-modify-write is intentional here.</b> {@code TwinFieldRecomputeService}
     * and the cascade-recompute pipeline operate on {@link TwinFieldDecimalEntity} in memory: they
     * need the materialized result ({@code current + delta}) to feed downstream field recalculations
     * within the same transaction. An atomic DB-side increment cannot supply that without a
     * read-back, and {@link FieldTyperDecimalIncrement} does not participate in the recompute
     * pipeline at all. Fields that drive cascades therefore need the value, not the atomicity.
     * <p>
     * <b>Trade-off accepted:</b> two concurrent {@code +N} writes to the same field can lose one
     * increment (classic lost update — no {@code @Version} / row lock on this entity). This is
     * acceptable for fields whose value feeds cascades and have low write contention; for
     * high-concurrency counters use {@link FieldTyperDecimalIncrement} instead. Gated by the
     * {@code allowIncrementValue} param (opt-in, default {@code false}).
     * <p>
     * The result runs through {@link FieldTyperNumeric#scaleAndCheckRange} for the same
     * {@code decimalPlaces}/{@code round} and {@code min}/{@code max} checks a plain value gets,
     * before {@link FieldTyperSingleValue#detectValueChange} persists it.
     */
    public BigDecimal processIncrementedValue(Properties properties, TwinFieldDecimalEntity twinFieldEntity, BigDecimal delta) throws ServiceException {
        return foldDelta(properties, twinFieldEntity.getTwinClassField(), twinFieldEntity.getValue(), delta);
    }

    /**
     * Shared delta-folding math used by both {@link #processIncrementedValue} and {@link #validate}:
     * {@code currentValue + delta} (or just {@code delta} when the field holds no value yet), then
     * scaled and range-checked. {@code currentValue} may be {@code null} — e.g. a new field with no
     * stored row yet during pre-flight validation.
     */
    private BigDecimal foldDelta(Properties properties, TwinClassFieldEntity twinClassField, BigDecimal currentValue, BigDecimal delta) throws ServiceException {
        BigDecimal result = currentValue != null ? currentValue.add(delta) : delta;
        return scaleAndCheckRange(properties, twinClassField, result);
    }

    /**
     * True iff {@code rawValue} is a signed {@code +/-} delta to fold into the current value (not an
     * absolute set). A bare {@code "0"} / {@code "+0"} / {@code "-0"} is <b>not</b> an increment —
     * zero must zero the field, so it falls through to the absolute path and preserves the legacy
     * {@code setValue(0)} semantics.
     */
    private boolean isIncrementInput(Properties properties, String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(allowIncrementValue.extract(properties))
                && DELTA_PATTERN.matcher(rawValue).matches()
                && new BigDecimal(rawValue).signum() != 0;
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldDecimalEntity twinFieldEntity, BigDecimal newValue) {
        return historyService.fieldChangeDecimal(
                twinFieldEntity.getTwinClassField(),
                twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null,
                newValue
        );
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchNumeric search) throws ServiceException {
        return TwinSpecification.checkFieldDecimal(search);
    }

    @Override
    protected ValidationResult validate(Properties properties, TwinEntity twin, FieldValueText fieldValue) throws ServiceException {
        var ret = new ValidationResult(true);
        try {
            var rawValue = fieldValue.getValue();
            if (isIncrementInput(properties, rawValue)) {
                // Validate the RESULT (current + delta) against range/scale — same path processValue takes.
                TwinFieldDecimalEntity twinFieldEntity = resolveTwinFieldEntity(twin, fieldValue.getTwinClassField());
                BigDecimal currentValue = twinFieldEntity != null ? twinFieldEntity.getValue() : null;
                foldDelta(properties, fieldValue.getTwinClassField(), currentValue, new BigDecimal(rawValue));
            } else {
                processAndFormatValue(properties, fieldValue);
            }
        } catch (ServiceException e) {
            ret.setValid(false).addMessage(e.getMessage());
        }
        return ret;
    }
}
