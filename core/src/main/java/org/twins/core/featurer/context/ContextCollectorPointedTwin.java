package org.twins.core.featurer.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twinpointer.TwinPointerService;

import java.util.Properties;
import java.util.UUID;

/**
 * Collects from the twin resolved via a twin pointer (id, name, description) — the pointer follows
 * the configured hops (head, links, ...) relative to the root twin. A null pointed twin is a config
 * error and fails the collection ({@link ErrorCodeTwins#POINTER_ON_NULL}).
 */
@Component
@Featurer(id = FeaturerTwins.ID_4912,
        name = "Context collector pointed twin",
        description = "Collect from twin resolved via twin pointer (id, name, description)")
@Slf4j
public class ContextCollectorPointedTwin extends ContextCollectorTwinBase {

    @FeaturerParam(name = "Twin pointer id", description = "Pointer resolving the target twin relative to the root twin", order = 0)
    public static final FeaturerParamUUID twinPointerId = new FeaturerParamUUID("twinPointerId");

    @Lazy
    @Autowired
    private TwinPointerService twinPointerService;

    /**
     * Bulk-load pointed twins for the whole batch so {@link #resolveTwin} reads the pointer cache
     * in-memory (one pointer resolution per twin would be N+1).
     */
    @Override
    protected void beforeCollect(ContextCollectorBatch batch, Properties properties) throws ServiceException {
        if (!batch.getTwins().isEmpty()) {
            twinPointerService.loadPointer(batch.getTwins(), twinPointerId.extract(properties));
        }
    }

    @Override
    protected TwinEntity resolveTwin(ContextRoot root, Properties properties) throws ServiceException {
        UUID pointerId = twinPointerId.extract(properties);
        TwinEntity pointed = root.getTwin().getPointer(pointerId);
        if (pointed == null) {
            throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL, "configured twin pointer " + pointerId + " is pointed on null");
        }
        return pointed;
    }
}
