package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * One hop of the batch twin navigation — see ai/plans/lookuper-navigation-stages.md. A stage first
 * bulk-loads what it needs for the whole frontier ({@link #load}), then transitions each item to
 * its next twin in memory ({@link #next}, isolated per item by the engine).
 * <p>
 * A stage is a small immutable VALUE of the hop it represents — its parameter (e.g. the link field
 * id) is part of the stage itself. Stages are constructed per {@code lookupFieldValue} call, which
 * runs ONCE PER BATCH, so the few allocations per batch are noise next to the engine's own
 * collections. The driving {@link FieldLookuperNavigated} engine is passed into both methods and
 * carries the services (so unit tests keep injecting the service mocks into the lookuper).
 */
public abstract class NavigationStage {

    /** Hop to the twin's head. */
    public static NavigationStage head() {
        return new Head();
    }

    /** Hop through a link FIELD: the twin's freshest value of the field is a single-item FieldValueLink. */
    public static NavigationStage linkField(UUID linkFieldId) {
        return new LinkField(linkFieldId);
    }

    /** Hop through a link FIELD of the loaded field-values kit (no freshest/uncommitted lookups). */
    public static NavigationStage valuesLinkField(UUID linkFieldId) {
        return new ValuesLinkField(linkFieldId);
    }

    /** Hop through a forward LINK of the given id: the first matched link's dst twin. */
    public static NavigationStage forwardLink(UUID linkId) {
        return new ForwardLink(linkId);
    }

    /** Bulk-load what {@link #next} needs for the whole frontier — one query per stage per batch. */
    public abstract void load(FieldLookuperNavigated engine, Collection<TwinEntity> frontier) throws ServiceException;

    /** In-memory transition to the next twin of the chain; may throw to fail the item. */
    public abstract TwinEntity next(FieldLookuperNavigated engine, TwinEntity twin, FactoryContext factoryContext) throws ServiceException;

    private static final class Head extends NavigationStage {
        @Override
        public void load(FieldLookuperNavigated engine, Collection<TwinEntity> frontier) throws ServiceException {
            engine.twinService.loadHead(frontier); // one bulk head load for the whole frontier
        }

        @Override
        public TwinEntity next(FieldLookuperNavigated engine, TwinEntity twin, FactoryContext factoryContext) throws ServiceException {
            TwinEntity headTwin = twin.getHeadTwin();
            if (headTwin == null) // structural error — the twin has no head, not a missing value
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "head twin is not detected for " + twin.logShort());
            return headTwin;
        }
    }

    private static final class LinkField extends NavigationStage {
        private final UUID linkFieldId;

        private LinkField(UUID linkFieldId) {
            this.linkFieldId = linkFieldId;
        }

        @Override
        public void load(FieldLookuperNavigated engine, Collection<TwinEntity> frontier) throws ServiceException {
            engine.twinService.loadTwinFields(frontier); // one bulk load for the freshest read of the link field
        }

        @Override
        public TwinEntity next(FieldLookuperNavigated engine, TwinEntity twin, FactoryContext factoryContext) throws ServiceException {
            FieldValue linkField = engine.getFreshestValue(twin, linkFieldId, factoryContext);
            if (linkField == null) // navigation field missing — the chain cannot even leave this twin
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + linkFieldId + "] is not found for navigation from " + twin.logShort());
            return FieldValueLink.getSingleLinkedTwinSafe(linkField);
        }
    }

    private static final class ValuesLinkField extends NavigationStage {
        private final UUID linkFieldId;

        private ValuesLinkField(UUID linkFieldId) {
            this.linkFieldId = linkFieldId;
        }

        @Override
        public void load(FieldLookuperNavigated engine, Collection<TwinEntity> frontier) throws ServiceException {
            engine.twinService.loadFieldsValues(frontier); // one bulk load of the field-values kits
        }

        @Override
        public TwinEntity next(FieldLookuperNavigated engine, TwinEntity twin, FactoryContext factoryContext) throws ServiceException {
            FieldValue linkField = twin.getFieldValuesKit().get(linkFieldId);
            return FieldValueLink.getSingleLinkedTwinSafe(linkField);
        }
    }

    private static final class ForwardLink extends NavigationStage {
        private final UUID linkId;

        private ForwardLink(UUID linkId) {
            this.linkId = linkId;
        }

        @Override
        public void load(FieldLookuperNavigated engine, Collection<TwinEntity> frontier) throws ServiceException {
            engine.twinLinkService.loadTwinLinks(frontier); // one bulk load of the links
            var matchedLinks = new ArrayList<TwinLinkEntity>();
            for (TwinEntity twin : frontier)
                // getGrouped returns an empty list for a missing linkId — next keeps its own error reporting
                matchedLinks.addAll(twin.getTwinLinks().getForwardLinks().getGrouped(linkId));
            if (!matchedLinks.isEmpty())
                engine.twinLinkService.loadDstTwin(matchedLinks); // one bulk load of the matched dst twins
        }

        @Override
        public TwinEntity next(FieldLookuperNavigated engine, TwinEntity twin, FactoryContext factoryContext) throws ServiceException {
            var links = twin.getTwinLinks().getForwardLinks().getGrouped(linkId);
            if (links.isEmpty())
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "no forward link[" + linkId + "] found from " + twin.logShort());
            return links.getFirst().getDstTwin();
        }
    }
}
