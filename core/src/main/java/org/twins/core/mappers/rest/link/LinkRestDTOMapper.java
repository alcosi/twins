package org.twins.core.mappers.rest.link;


import org.twins.core.mappers.rest.MapperMode;

public class LinkRestDTOMapper {
    public enum Mode implements MapperMode {
        SHORT, DETAILED, HIDE;

        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
    }
}
