package org.twins.core.domain;

/**
 * Interface for entities used in EntitiesChangesCollector and for everything postponable into
 * MapperContext related objects: the generic parameter is the id type (UUID for regular entities,
 * Integer/String for featurer/historyType and friends). Transport value objects with composite
 * identity (e.g. FeaturerParams) can not implement it because of setId.
 */
public interface Identifiable<ID> {
    ID getId();

    // for Accessor(chain = true)
    Identifiable<ID> setId(ID id);
}
