package org.twins.core.dao.face;

import org.cambium.common.EasyLoggable;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

/**
 * Marker contract for face variant entities. Carries the mandatory entity interfaces
 * (see entity_code_convention.md §15): implementors get {@link EasyLoggable} and
 * {@link Identifiable}&lt;UUID&gt; for free and must not redeclare them.
 */
public interface FaceVariantEntity extends EasyLoggable, Identifiable<UUID> {
    UUID getTwinPointerValidatorRuleId();
}
