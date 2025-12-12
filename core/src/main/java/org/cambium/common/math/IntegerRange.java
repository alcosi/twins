package org.cambium.common.math;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class IntegerRange {
    private Integer from;
    private Integer to;
}
