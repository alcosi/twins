package org.cambium.featurer.params;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@FeaturerParamType(
        id = "LIST_OF_MAPS",
        description = "json style list of key/value maps",
        regexp = ".*", //todo correct regexp
        example = "[{\"name\": \"John\", \"surname\": \"Doe\"}, {\"name\": \"Jack\", \"surname\": \"Black\"}]"
)
public class FeaturerParamListOfMaps extends FeaturerParam<List<Map<String, String>>>{

    final ObjectMapper objectMapper = new ObjectMapper();
    TypeReference<List<Map<String, String>>> typeRef = new TypeReference<>() {};

    public FeaturerParamListOfMaps(String key) {
        super(key);
    }

    @SneakyThrows
    @Override
    public List<Map<String, String>> extract(Properties properties) {
        return objectMapper.readValue(properties.get(key).toString(), typeRef);
    }
}
