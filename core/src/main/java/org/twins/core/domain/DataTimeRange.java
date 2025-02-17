package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class DataTimeRange {
    private Timestamp from;
    private Timestamp to;
}
