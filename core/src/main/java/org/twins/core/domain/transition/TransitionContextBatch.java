package org.twins.core.domain.transition;

import lombok.Getter;
import lombok.Setter;
import org.twins.core.domain.factory.FactoryResultUncommited;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class TransitionContextBatch {
    private final Collection<TransitionContext> all = new ArrayList<>();
    private final Collection<TransitionContext> simple = new ArrayList<>();
    private final Map<TransitionContext, FactoryResultUncommited> factoried = new HashMap<>();
    @Setter
    boolean mustBeDrafted = false;

    public TransitionContextBatch(Collection<TransitionContext> values) {
        for (TransitionContext transitionContext : values) {
            add(transitionContext);
        }
    }

    public TransitionContextBatch add(TransitionContext transitionContext) {
        all.add(transitionContext);
        if (transitionContext.getTransitionEntity().getInbuiltTwinFactoryId() != null)
            factoried.put(transitionContext, null);
        else
            simple.add(transitionContext);
        return this;
    }
}
