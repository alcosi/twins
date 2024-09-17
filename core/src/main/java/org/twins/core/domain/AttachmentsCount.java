package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttachmentsCount {
    private int all;
    private int direct;
    private int fromTransitions;
    private int fromComments;
    private int fromFields;
}
