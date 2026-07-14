package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_23,
        name = "Filler",
        description = "")
@Slf4j
public abstract class Filler extends FeaturerTwins {
    @Lazy
    @Autowired
    FieldLookupers fieldLookupers;

    public void fill(HashMap<String, String> fillerParams, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, String logMsg, boolean optional) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fillerParams);
        log.info(logMsg + ": running filler[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        fill(properties, factoryItems, templateTwin, optional);
    }

    /**
     * Bulk fill: processes the whole collection of factory items in a single call.
     * <p>
     * This signature forces every filler to support batch processing and eliminates
     * per-item N+1 query risks — any data needed from the DB must be preloaded in bulk
     * for the whole collection, not fetched once per item. Fillers that can batch their
     * DB access should override this method directly and preload data for all items up
     * front; fillers with purely independent per-item logic may delegate to
     * {@link #fillEach} and only override {@link #fillItem}.
     * <p>
     * Error contract (must be honored by every implementation):
     * <ul>
     *   <li>When {@code optional == true} and {@link #canBeOptional()} is {@code true}, a
     *       single failing item must NOT break the rest of the batch — log it and continue
     *       with the remaining items ("skip the bad one, keep the good ones").</li>
     *   <li>When the step is mandatory ({@code optional == false}, or
     *       {@link #canBeOptional()} is {@code false}), a failing item must propagate the
     *       exception so the service layer aborts the whole factory.</li>
     * </ul>
     * {@link #fillEach} implements this contract correctly for the independent-per-item
     * case; fillers overriding this method directly must implement it themselves.
     */
    public abstract void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException;

    /**
     * Convenience helper for fillers whose per-item logic is independent (no cross-item
     * batching needed). Loops the collection applying {@link #fillItem} to each item,
     * honoring the per-item error contract documented on {@link #fill}: a failing item is
     * logged and skipped when {@code optional && canBeOptional()}, otherwise the exception
     * is rethrown (mandatory step aborts the factory).
     */
    protected void fillEach(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        for (FactoryItem factoryItem : factoryItems) {
            try {
                fillItem(properties, factoryItem, templateTwin);
            } catch (Exception ex) {
                if (optional && canBeOptional()) {
                    log.warn("Optional filler step failed for {}, skipping: {}", factoryItem.logShort(), (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()));
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * Per-item logic. Override this (and delegate from {@link #fill} via {@link #fillEach})
     * for the simple independent-per-item case, OR override {@link #fill} directly for
     * batched logic. Default implementation throws to fail fast on misconfiguration.
     */
    protected void fillItem(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        throw new UnsupportedOperationException("fillItem is not implemented: either override fillItem (and use fillEach) or override fill(...) directly");
    }

    public boolean canBeOptional() {
        return true; // most steps can be option by default. otherwise method must be overridden
    }


}
