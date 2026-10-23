package org.twins.core.featurer.context;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Objects;

/**
 * Root object a {@link ContextCollector} collects data from. Always carries a twin — head twin,
 * pointed twin, twin class, attachments, users etc. are resolved relative to it. Subclasses add
 * source-specific payload (e.g. {@link ContextRootHistory} adds the history record); other source
 * kinds may appear later (factory pipeline, face request), but every one has a twin.
 * <p>Identity: plain roots are equal by twin id; {@link ContextRootHistory} roots are equal by the
 * history record (two histories of the same twin are distinct roots — history-scoped collectors may
 * produce different values for them).
 */
@Getter
public class ContextRoot {
    protected final TwinEntity twin;

    public ContextRoot(TwinEntity twin) {
        this.twin = twin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(twin.getId(), ((ContextRoot) o).twin.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(twin.getId());
    }
}
