package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttachmentsCount {
    private Integer all;
    private Integer direct;
    private Integer fromTransitions;
    private Integer fromComments;
    private Integer fromFields;
}
