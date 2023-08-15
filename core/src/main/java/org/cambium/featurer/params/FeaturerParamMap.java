package org.cambium.featurer.params;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cambium.featurer.annotations.FeaturerParamType;
import lombok.SneakyThrows;

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
    TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<HashMap<String, String>>() {
    };

    public FeaturerParamMap(String key) {
        super(key);
    }

    @SneakyThrows
    @Override
    public Map<String, String> extract(Properties properties) {
        Map<String, String> map = objectMapper.readValue(properties.get(key).toString(), typeRef);
        return map;
    }
}
