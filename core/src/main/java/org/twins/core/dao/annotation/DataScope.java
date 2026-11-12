package org.twins.core.dao.annotation;

import org.twins.core.enums.datascope.DataScopeType;

import java.lang.annotation.*;

/**
 * Declares which data scopes the annotated entity's table may hold. A table that mixes rows of
 * different scopes declares several values (e.g. {@code @DataScope({SYSTEM, DOMAIN})} — global
 * system rows plus per-domain rows in the same table).
 * <p>
 * Consumers:
 * <ul>
 *   <li>domain settings duplicate — entities holding {@link DataScopeType#DOMAIN} rows are copy
 *       candidates, {@link DataScopeType#SYSTEM} / {@link DataScopeType#DOMAIN_MEMBER} rows are not;</li>
 *   <li>future business account data export — entities holding
 *       {@link DataScopeType#BUSINESS_ACCOUNT} rows are filterable by {@code business_account_id};</li>
 *   <li>{@code DataScopeAnnotationCoverageTest} keeps the classification complete and consistent
 *       with the actual {@code domain_id} / {@code business_account_id} columns.</li>
 * </ul>
 * <p>
 * Classification rules (applied to the physical column, not the JPA field):
 * <ul>
 *   <li>{@code domain_id NOT NULL} → {@code {DOMAIN}} — every row belongs to a domain;</li>
 *   <li>{@code domain_id} nullable → {@code {DOMAIN, SYSTEM}} — the table holds global rows
 *       ({@code domain_id IS NULL}) alongside domain rows (e.g. {@code twin_class},
 *       {@code storage}, {@code data_list}, {@code i18n});</li>
 *   <li>no own {@code domain_id}, scoped via parent FK (e.g. {@code twin_class_field} →
 *       {@code twin_class}) → inherits the parent's scope: parent {@code {DOMAIN, SYSTEM}} ⇒
 *       child {@code {DOMAIN, SYSTEM}}, parent {@code {DOMAIN}} ⇒ child {@code {DOMAIN}};</li>
 *   <li>user / member generated data (twins, drafts, history, comments, memberships, resources) →
 *       {@code DOMAIN_MEMBER} (+ {@code SYSTEM} where the scoping column is nullable and global
 *       rows exist);</li>
 *   <li>{@code business_account_id} column present → add {@code BUSINESS_ACCOUNT}.</li>
 * </ul>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    DataScopeType[] value();
}
