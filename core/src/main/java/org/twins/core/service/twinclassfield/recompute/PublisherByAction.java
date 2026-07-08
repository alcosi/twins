package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.action.TwinAction;

/**
 * Publisher for an OnAction recompute: a {@link TwinAction} was performed on
 * {@code twin}. TwinAction is an enum (no extra lookup); the twin entity is
 * already loaded.
 */
public record PublisherByAction(
        TwinEntity twin,
        TwinAction action,
        boolean async
) implements PublisherRef {}
