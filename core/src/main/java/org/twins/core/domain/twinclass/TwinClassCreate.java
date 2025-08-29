package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassCreate extends TwinClassSave {
    private Boolean autoCreatePermission;
    private Boolean autoCreateTwinflow;
}
