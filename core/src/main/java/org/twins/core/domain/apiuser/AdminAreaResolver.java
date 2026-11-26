package org.twins.core.domain.apiuser;

/**
 * Resolves whether the current context is the FE admin zone.
 * <p><b>Not a security boundary:</b> the underlying signal (the {@code AdminArea} header) is
 * client-controlled — any holder of the gated permission can set it. It only narrows where
 * elevated permissions (e.g. DOMAIN_TWINS_VIEW_ALL) take effect; never use it as the sole
 * basis for a security decision.
 */
public interface AdminAreaResolver {
    boolean resolveAdminArea();
}
