package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class LongRange {
    private Long from;
    private Long to;
}