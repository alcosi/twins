package org.twins.core.dao.datalist;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class DataListOptionSubsetId implements Serializable {
    private UUID dataListSubsetId;
    private UUID dataListOptionId;
}
