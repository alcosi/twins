package org.twins.core.enums.twinclass;

import lombok.Getter;

@Getter
public enum LogicOperator {
    AND("AND"), OR("OR"), LEAF("LEAF");

    private final String id;

    LogicOperator(String id) {
        this.id = id;
    }
}
