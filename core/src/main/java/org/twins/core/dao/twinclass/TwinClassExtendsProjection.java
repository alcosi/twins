package org.twins.core.dao.twinclass;

import java.util.UUID;

public interface TwinClassExtendsProjection {
    UUID getId();
    String getExtendsHierarchyTree();
}