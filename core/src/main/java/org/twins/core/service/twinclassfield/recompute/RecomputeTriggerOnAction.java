package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.action.TwinAction;

/**
 * Trigger for an OnAction recompute: a {@link TwinAction} was performed on {@code publisherTwin}.
 * TwinAction is an enum (no extra lookup); the twin entity is already loaded.
 */
public record RecomputeTriggerOnAction(
        TwinEntity publisherTwin,
        TwinAction action,
        boolean async
) implements RecomputeTrigger {}
