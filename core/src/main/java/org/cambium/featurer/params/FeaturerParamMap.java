package org.cambium.featurer.params;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@FeaturerParamType(
        id = "MAP",
        description = "json style key/value map",
        regexp = ".*", //todo correct regexp
        example = "{\"name\": \"John\", \"surname\": \"Doe\"}")
public class FeaturerParamMap extends FeaturerParam<Map<String, String>> {
    final ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {};

    public FeaturerParamMap(String key) {
        super(key);
    }

    @SneakyThrows
    @Override
    public Map<String, String> extract(Properties properties) {
        return properties.get(key) == null ? new HashMap<>() : objectMapper.readValue(properties.get(key).toString(), typeRef);
    }
}
