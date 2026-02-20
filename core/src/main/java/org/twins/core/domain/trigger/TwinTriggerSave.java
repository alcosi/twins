package org.twins.core.domain.trigger;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class TwinTriggerSave {
    private Integer triggerFeaturerId;
    private Map<String, String> triggerParams;
    private String name;
    private String description;
    private Boolean active;
}
