package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Item {
    private String label;
    private String key;
    private int percent;
    private String colorHex;
}
