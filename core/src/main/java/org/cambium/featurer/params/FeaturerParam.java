package org.cambium.featurer.params;

import lombok.Data;

import java.util.Properties;

@Data
public abstract class FeaturerParam<T> {
    protected String key;

    public FeaturerParam(String key) {
        this.key = key;
    }

    public abstract T extract(Properties properties);
}
