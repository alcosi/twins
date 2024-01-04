package org.twins.core.mappers.rest.link;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.twins.core.mappers.rest.MapperMode;

public class LinkRestDTOMapper {
    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
