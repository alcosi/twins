package org.twins.core.dto.rest.related;

public @interface RelatedObject {
    Class<?> type();
    String name();
}
