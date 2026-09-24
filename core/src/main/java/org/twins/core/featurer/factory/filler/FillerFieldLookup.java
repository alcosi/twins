package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;
import java.util.UUID;

/**
 * Per-item {@link Filler} driven by ONE batch field lookup: the {@link FieldLookuperNearest} batch
 * entry runs once per step (bulk preloads + entity resolution once, per-item failures isolated into
 * {@link LookupResult#failures()}), then {@link #fill(Properties, FactoryItem, TwinEntity, FieldValue)}
 * is invoked per factory item with the pre-resolved value. The item's own lookup failure is
 * re-thrown inside the loop, so the optional-step semantics are identical to the old per-item
 * caller: a failing item of an optional step is logged and skipped, a mandatory failure aborts.
 * <p>The value source is the {@code fieldLookuper} step param — each concrete filler declares its
 * own {@link FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam} with the appropriate
 * {@code defaultValue}, so subclasses that used to exist only to pin a different lookuper are
 * replaced by a param value on this single filler.
 */
@Slf4j
public abstract class FillerFieldLookup extends Filler {

    @Override
    public final void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        LookupResult result = lookuper(properties).lookupFieldValue(batch, lookupFieldId(properties));
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem);
                fill(properties, factoryItem, templateTwin, result.value(factoryItem));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }

    /**
     * Value source of this filler, resolved from the filler's own {@code fieldLookuperParam} —
     * each concrete filler declares the param (with its default value) and binds it here:
     * <pre>{@code
     * @Override
     * protected FieldLookuperNearest lookuper(Properties properties) {
     *     return (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
     * }
     * }</pre>
     */
    protected abstract FieldLookuperNearest lookuper(Properties properties);

    /** Field id to look up, extracted from the step params once per batch. */
    protected abstract UUID lookupFieldId(Properties properties) throws ServiceException;

    /** Per-item fill with the pre-resolved lookup value. */
    public abstract void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException;
}
