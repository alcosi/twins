package org.twins.core.domain.datalist;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataListCreate extends DataListSave {
    private DataListOptionCreate defaultOption;
}
