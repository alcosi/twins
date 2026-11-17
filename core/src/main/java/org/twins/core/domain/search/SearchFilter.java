package org.twins.core.domain.search;

import java.lang.annotation.*;

/**
 * Marks a domain-search field as a client-narrowable filter field: the configured-search flow
 * (ConfiguredSearchService.narrowSearch) narrows it by intersection, scanning annotated fields
 * once per class. Narrowed field types: {@code Set}, {@code Ternary}, {@code Map<UUID,Boolean>}.
 * Fields of any other type (e.g. ranges, hierarchy objects) are skipped — the annotation marks
 * them as filter fields, but narrowing is not implemented for them yet. For narrowing that the
 * generic scan cannot express, override ConfiguredSearchService.narrowSearchSpecific in the
 * config service.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SearchFilter {
}
